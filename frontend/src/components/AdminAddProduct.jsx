import React, { useState } from "react";
import { addProduct } from "../api/products";

const AdminAddProduct = ({ token }) => {
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [price, setPrice] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const product = { name, description, price };
      const savedProduct = await addProduct(product, token);
      alert("Produkt přidán: " + savedProduct.name);
      setName("");
      setDescription("");
      setPrice("");
    } catch (err) {
      alert("Chyba: " + err.message);
    }
  };

  return (
    <form
      onSubmit={handleSubmit}
      className="flex flex-col gap-4 max-w-md mx-auto p-4 border rounded-lg shadow"
    >
      <h2 className="text-xl font-bold">Přidat nový produkt</h2>
      <input
        type="text"
        placeholder="Název produktu"
        value={name}
        onChange={(e) => setName(e.target.value)}
        className="border p-2 rounded"
        required
      />
      <textarea
        placeholder="Popis produktu"
        value={description}
        onChange={(e) => setDescription(e.target.value)}
        className="border p-2 rounded"
        required
      />
      <input
        type="number"
        placeholder="Cena"
        value={price}
        onChange={(e) => setPrice(e.target.value)}
        className="border p-2 rounded"
        required
      />
      <button
        type="submit"
        className="bg-blue-600 text-white py-2 rounded hover:bg-blue-700"
      >
        Přidat produkt
      </button>
    </form>
  );
};

export default AdminAddProduct;