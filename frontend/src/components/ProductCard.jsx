import React from "react";

const ProductCard = ({ name, description, price, image }) => {
  return (
    <div className="border rounded-lg shadow-md p-4 w-60 bg-white hover:shadow-lg transition">
      <img src={image} alt={name} className="w-full h-40 object-cover rounded-md" />
      <h2 className="text-lg font-bold mt-2">{name}</h2>
      <p className="text-sm text-gray-600">{description}</p>
      <p className="text-blue-600 font-semibold mt-2">{price} Kč</p>
      <button className="mt-3 bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600">
        Přidat do košíku 🛒
      </button>
    </div>
  );
};

export default ProductCard;