import React from "react";

const Menu = () => {
  return (
    <nav style={{
      display: "flex",
      gap: "40px",
      justifyContent: "center",
      padding: "10px",
      background: "#f4f4f4",
      border: "1px solid #ddd",
      padding: "15px",
        marginBottom: "20px",
        fontSize: "35px",
        borderRadius: "15px"
    }}>
      <a href="#" style={{ textDecoration: "none", color: "black"}}>Elektronika</a>
      <a href="#" style={{ textDecoration: "none", color: "black"}}>Oblečení</a>
      <a href="#" style={{ textDecoration: "none", color: "black"}}>Novinky</a>
      <a href="#" style={{ textDecoration: "none", color: "black"}}>Kontakt</a>
    </nav>
  );
};

export default Menu;