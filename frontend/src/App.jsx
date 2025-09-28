import { useEffect, useState } from 'react';
import { fetchProducts } from './api/productService';

function App() {
  const [products, setProducts] = useState([]);

  useEffect(() => {
    fetchProducts().then(data => setProducts(data));
  }, []);

  return (
    <div>
      <h1>Produkty</h1>
      <ul>
        {products.map(p => (
          <li key={p.id}>{p.name} - {p.price}</li>
        ))}
      </ul>
    </div>
  );
}

export default App
