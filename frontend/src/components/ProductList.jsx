import React, { useEffect, useState } from "react";
import ProductCard from "./ProductCard";
import { fetchProducts } from "../api/productService";

const ProductList = () => {
     const [products, setProducts] = useState([]);
      const [loading, setLoading] = useState(true);

      useEffect(() => {
        fetchProducts()
          .then((data) => {
            setProducts(data);
            setLoading(false);
          })
          .catch((error) => {
            console.error("Chyba:", error);
            setLoading(false);
          });
      }, []);

      if (loading) {
        return <p>Načítám produkty...</p>;
      }

      return (
        <div className="grid grid-cols-4 gap-6 p-6">
          {products.map((p) => (
            <ProductCard
              key={p.id}
              name={p.name}
              description={p.description}
              price={p.price}
              image={"https://via.placeholder.com/150"}
            />
          ))}
        </div>
      );
    };


export default ProductList;