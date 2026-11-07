import streamlit as st
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt

st.set_page_config(page_title="EV-EnergySync", page_icon="🔋", layout="wide")
st.title("🔋 EV-EnergySync")
st.subheader("Intelligent Smart Charging and Bidirectional Energy Management")
st.warning("All profiles and results are illustrative software-simulation data, not measured experimental results. This is not a real charger controller.")

with st.sidebar:
    st.header("Simulation settings")
    cap = st.slider("Battery capacity (kWh)", 20, 120, 60, 5)
    initial = st.slider("Initial SOC (%)", 20, 95, 65, 5)
    reserve = st.slider("Protected SOC reserve (%)", 10, 70, 40, 5)
    maximum = st.slider("Maximum SOC (%)", 70, 100, 90, 5)
    target = st.slider("Departure SOC target (%)", 40, 100, 70, 5)
    pcharge = st.slider("Max charging power (kW)", 1.0, 11.0, 3.0, 0.5)
    pdis = st.slider("Max discharge/export power (kW)", 1.0, 11.0, 2.0, 0.5)
    ec = st.slider("Charging efficiency (%)", 80, 100, 92) / 100
    ed = st.slider("Discharging efficiency (%)", 80, 100, 92) / 100
    credit = st.slider("Grid export credit (₹/kWh)", 0.0, 15.0, 5.0, 0.5)

if reserve >= maximum:
    st.error("Maximum SOC must be greater than the reserve.")
    st.stop()

h = np.arange(24)
load = np.array([1.2,1.0,.9,.8,.9,1.2,1.8,2.5,2.8,2.2,2,1.8,1.7,1.9,2,2.2,2.8,3.2,3.5,3.3,2.8,2.4,1.9,1.5])
pv = np.array([0,0,0,0,0,0,0,.3,.8,1.5,2,2.5,2.8,2.5,2,1.2,.5,0,0,0,0,0,0,0])
tariff = np.array([4,4,4,4,4,4,5,6,7,7,5,4,4,4,5,6,8,9,9,8,7,6,5,4])

def run(mode_strategy):
    soc = float(initial)
    rows = []
    for t in h:
        charge = v2h = v2g = 0.0
        # PV is allocated to household demand first.
        pv_home = min(pv[t], load[t])
        pv_surplus = max(0.0, pv[t] - pv_home)
        net_load = max(0.0, load[t] - pv_home)
        mode = "Idle"
        if mode_strategy == "Baseline":
            if t <= 7 and soc < maximum:
                charge = min(pcharge, max(0, (maximum-soc)*cap/100/max(ec, 1e-9)))
                mode = "G2V" if charge > 0 else "Idle"
        else:
            headroom = max(0, (maximum-soc)*cap/100/max(ec, 1e-9))
            gap = max(0, (target-soc)*cap/100)
            if pv_surplus > 0 and headroom > 0:
                charge = min(pcharge, pv_surplus, headroom)
                mode = "G2V" if charge > 0 else "Idle"
            elif t <= 6 and gap > 0 and headroom > 0:
                hours_left = max(1, 7-t)
                charge = min(pcharge, headroom, gap/(ec*hours_left))
                mode = "G2V" if charge > 0 else "Idle"
            elif t == 17 and soc > max(reserve, target) + 10:
                available = max(0, (soc-max(reserve, target))*cap/100)
                v2g = min(pdis, available*ed)
                mode = "V2G" if v2g > 0 else "Idle"
            elif t in (18,19,20) and soc > reserve:
                available = max(0, (soc-reserve)*cap/100)
                v2h = min(pdis, net_load, available*ed)
                mode = "V2H" if v2h > 0 else "Idle"
        # Charging and discharging are never simultaneous.
        if charge > 0:
            v2h = v2g = 0.0
        soc += (charge*ec - (v2h+v2g)/max(ed, 1e-9))/cap*100
        soc = min(maximum, max(0, soc))
        grid = load[t] + charge - v2h - v2g - pv[t]
        imp, exp = max(0, grid), max(0, -grid)
        cost = imp*tariff[t] - exp*credit
        rows.append([t,load[t],pv[t],tariff[t],mode,charge,v2h,v2g,grid,soc,imp,exp,cost])
    df = pd.DataFrame(rows, columns=["Hour","Household load (kW)","PV generation (kW)","Tariff (₹/kWh)","Mode","G2V charge (kW)","V2H supply (kW)","V2G export (kW)","Grid power (kW)","SOC (%)","Grid import (kWh)","Grid export (kWh)","Cost (₹)"])
    summary = {
        "Imported energy (kWh)": df["Grid import (kWh)"].sum(),
        "Exported energy (kWh)": df["Grid export (kWh)"].sum(),
        "G2V energy (kWh)": df["G2V charge (kW)"].sum(),
        "V2H energy (kWh)": df["V2H supply (kW)"].sum(),
        "V2G energy (kWh)": df["V2G export (kW)"].sum(),
        "Peak grid import (kW)": max(0, df["Grid power (kW)"].max()),
        "Net energy cost (₹)": df["Cost (₹)"].sum(),
        "Final SOC (%)": df["SOC (%)"].iloc[-1],
    }
    return df, summary

base, bs = run("Baseline")
smart, ss = run("Smart")
saving = bs["Net energy cost (₹)"] - ss["Net energy cost (₹)"]
pct = saving / bs["Net energy cost (₹)"] * 100 if bs["Net energy cost (₹)"] else 0

a,b,c,d = st.columns(4)
a.metric("Smart-schedule net cost", f'₹{ss["Net energy cost (₹)"]:.2f}')
b.metric("Cost difference vs baseline", f"₹{saving:.2f}", f"{pct:.1f}%")
c.metric("Peak grid import", f'{ss["Peak grid import (kW)"]:.2f} kW', f'{bs["Peak grid import (kW)"]:.2f} kW baseline')
d.metric("Final SOC", f'{ss["Final SOC (%)"]:.1f}%')

t1,t2,t3,t4 = st.tabs(["Dashboard","Operating modes","Cost comparison","Data & downloads"])
with t1:
    col1,col2 = st.columns(2)
    with col1:
        fig,ax=plt.subplots(figsize=(8,4))
        ax.plot(h,smart["SOC (%)"],marker="o",label="SOC")
        ax.axhline(reserve,ls="--",label=f"Reserve {reserve}%")
        ax.axhline(maximum,ls="--",label=f"Maximum {maximum}%")
        ax.axhline(target,ls=":",label=f"Departure target {target}%")
        ax.set(xlabel="Hour",ylabel="SOC (%)",title="Battery State of Charge"); ax.set_xticks(range(0,24,2)); ax.grid(alpha=.3); ax.legend(); fig.tight_layout(); st.pyplot(fig); plt.close(fig)
    with col2:
        fig,ax=plt.subplots(figsize=(8,4))
        ax.plot(h,smart["Household load (kW)"],label="Household load")
        ax.plot(h,smart["Grid power (kW)"],label="Net grid power")
        ax.plot(h,smart["PV generation (kW)"],label="PV generation")
        ax.set(xlabel="Hour",ylabel="Power (kW)",title="Load Balancing"); ax.set_xticks(range(0,24,2)); ax.grid(alpha=.3); ax.legend(); fig.tight_layout(); st.pyplot(fig); plt.close(fig)
with t2:
    fig,ax=plt.subplots(figsize=(11,4))
    for col,label in [("G2V charge (kW)","G2V charging"),("V2H supply (kW)","V2H supply"),("V2G export (kW)","V2G export")]:
        ax.step(h,smart[col],where="mid",label=label)
    ax.set(xlabel="Hour",ylabel="Power (kW)",title="G2V, Idle, V2H and V2G Schedule"); ax.set_xticks(range(24)); ax.grid(alpha=.3); ax.legend(); fig.tight_layout(); st.pyplot(fig); plt.close(fig)
    st.dataframe(smart[["Hour","Mode","SOC (%)","G2V charge (kW)","V2H supply (kW)","V2G export (kW)"]],use_container_width=True,hide_index=True)
with t3:
    comp=pd.DataFrame([{"Scenario":"Uncontrolled baseline",**bs},{"Scenario":"EV-EnergySync smart schedule",**ss}])
    st.dataframe(comp.round(2),use_container_width=True,hide_index=True)
    fig,ax=plt.subplots(figsize=(7,4)); ax.bar(["Baseline","EV-EnergySync"],[bs["Net energy cost (₹)"],ss["Net energy cost (₹)"]]); ax.set(ylabel="Net energy cost (₹)",title="Illustrative Daily Energy Cost"); ax.grid(axis="y",alpha=.3); fig.tight_layout(); st.pyplot(fig); plt.close(fig)
    fig,ax=plt.subplots(figsize=(7,4)); ax.bar(["Baseline","EV-EnergySync"],[bs["Peak grid import (kW)"],ss["Peak grid import (kW)"]]); ax.set(ylabel="Peak import (kW)",title="Peak Grid Import Comparison"); ax.grid(axis="y",alpha=.3); fig.tight_layout(); st.pyplot(fig); plt.close(fig)
with t4:
    st.dataframe(smart.round(3),use_container_width=True,hide_index=True)
    st.download_button("Download smart schedule CSV",smart.to_csv(index=False).encode("utf-8"),"ev_energysync_smart_schedule.csv","text/csv")
    st.download_button("Download baseline CSV",base.to_csv(index=False).encode("utf-8"),"ev_energysync_baseline.csv","text/csv")
    for title, filename, cols in [
        ("Battery SOC","battery_soc.png",["SOC (%)"]),
        ("Load balancing","load_balancing.png",["Household load (kW)","Grid power (kW)","PV generation (kW)"]),
        ("Operating modes","ev_operating_modes.png",["G2V charge (kW)","V2H supply (kW)","V2G export (kW)"]),
    ]:
        fig,ax=plt.subplots(figsize=(9,4))
        for col in cols: ax.plot(h,smart[col],label=col)
        ax.set(xlabel="Hour",ylabel="SOC (%)" if title=="Battery SOC" else "Power (kW)",title=title); ax.set_xticks(range(0,24,2)); ax.grid(alpha=.3); ax.legend(); fig.tight_layout()
        from io import BytesIO
        buf=BytesIO(); fig.savefig(buf,format="png",dpi=300,bbox_inches="tight"); plt.close(fig)
        st.download_button(f"Download {title} figure (300 DPI)",buf.getvalue(),filename,"image/png")

st.caption("Model assumptions: one-hour time steps, fixed illustrative profiles, simplified efficiency model, no battery degradation or grid-code simulation. Real V2H/V2G requires compatible certified bidirectional hardware, electrical protection, and grid/operator approval.")
