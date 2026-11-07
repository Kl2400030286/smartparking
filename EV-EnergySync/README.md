# EV-EnergySync Streamlit Dashboard

## Run on Windows
1. Install Python 3.10+ from https://www.python.org/downloads/
2. Extract this ZIP and open the `EV-EnergySync` folder in VS Code.
3. Open Terminal → New Terminal and run:

```bash
python -m venv .venv
.venv\\Scripts\\activate
python -m pip install -r requirements.txt
streamlit run app.py
```

If the browser does not open automatically, visit http://localhost:8501.

## Features
- G2V, Idle, V2H, and V2G simulation
- Protected SOC reserve and configurable battery settings
- Household load balancing and solar generation
- Baseline vs smart-schedule cost and peak import comparison
- Interactive charts, CSV exports, and 300-DPI PNG research figures

## Research integrity and safety
All load, solar, tariff, and output values are illustrative sample data, not measured findings. This is a software simulation, not a certified charger controller. Real V2H/V2G requires compatible certified bidirectional equipment, electrical protection, and grid/operator approval. Validate assumptions before making scientific claims.
