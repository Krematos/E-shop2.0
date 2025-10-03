import React from 'react';
import { Link } from "react-router-dom";

const headerStyle = {
  display: "flex",
  justifyContent: "space-between",
  alignItems: "center",
  padding: "10px 20px"
};

const logoStyle = {
  fontSize: "44px",
  fontWeight: "bold",
   color: "#191970"
};

const navStyle = {
  display: "flex",
  gap: "15px",
  fontSize: "20px",
};

const Header = () => {
  return (
    <header style={headerStyle}>
          <h1 style={logoStyle}>SecondEL</h1>

          <div style={{ textAlign: "center", margin: "20px" }}>
                      <input
                        type="text"
                        placeholder="Hledat produkty..."
                        style={{ width: "300px", padding: "8px" }}
                      />
                      <button style={{ marginLeft: "10px", padding: "8px 15px" }}>Hledat</button>
                    </div>

          <nav style={navStyle}>
            <Link to="/login" style={{ padding: "10px", textDecoration: "none", color: "black" }}>Přihlášení</Link>
            <Link to="/register" style={{ padding: "10px",  borderLeft: "3px solid #ddd", textDecoration: "none", color: "black" }}>Registrace</Link>
            <a href="#" style={{ padding: "10px",  borderRadius: "15px", background: "#f4f4f4", textDecoration: "none", color: "black" }}>🛒 Košík</a>
          </nav>


        </header>

  );
};

export default Header;
