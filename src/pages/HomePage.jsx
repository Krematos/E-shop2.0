import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getProducts } from '../services/productService';
import ProductCard from '../components/ProductCard';
import LoadingSpinner from '../components/LoadingSpinner';

const HomePage = () => {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [featuredProducts, setFeaturedProducts] = useState([]);

  useEffect(() => {
    const fetchProducts = async () => {
      try {
        setLoading(true);
        const data = await getProducts();

        console.log('Načtené produkty:', data);
        // 🛡️ OCHRANA: Ověříme, jestli je 'data' skutečně pole
                if (Array.isArray(data)) {
                  setProducts(data);

        // Zobrazíme první 6 produktů jako doporučené
        setFeaturedProducts(data.slice(0, 6));
        } else  if (data.content){
           console.error('Neočekávaný formát dat:', data);
           setProducts(data.content);
           setFeaturedProducts(data.content.slice(0, 6));
        }else {
          console.error('Neočekávaný formát dat:', data);
                    setProducts([]);
        }
      } catch (error) {
        console.error('Chyba při načítání produktů:', error);
        setProducts([]); // I při chybě sítě nastavíme prázdné pole
        setFeaturedProducts([]);
      } finally {
        setLoading(false);
      }
    };

    fetchProducts();
  }, []);

  const filteredProducts = products.filter((product) =>
    product.name.toLowerCase().includes(searchTerm.toLowerCase())
  );

  if (loading) {
    return <LoadingSpinner />;
  }

  return (
    <div className="min-h-screen">
      {/* Hero sekce */}
      <section className="bg-gradient-to-r from-primary-600 to-primary-800 text-white py-20">
        <div className="container mx-auto px-4 text-center">
          <h1 className="text-4xl md:text-5xl font-bold mb-4">
            Vítejte v našem E-Shopu
          </h1>
          <p className="text-xl mb-8 text-primary-100">
            Objevte široký výběr kvalitních produktů
          </p>
          
          {/* Vyhledávací lišta */}
          <div className="max-w-2xl mx-auto">
            <input
              type="text"
              placeholder="Hledat produkty..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full px-6 py-3 rounded-lg text-gray-900 focus:ring-2 focus:ring-white outline-none"
            />
          </div>
        </div>
      </section>

      {/* Kategorie */}
      <section className="container mx-auto px-4 py-12">
        <h2 className="text-3xl font-bold mb-8 text-center">Kategorie</h2>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          {['Elektronika', 'Oblečení', 'Domácnost', 'Sport'].map((category) => (
            <Link
              key={category}
              to={`/products?category=${category}`}
              className="card text-center hover:shadow-lg transition-shadow"
            >
              <div className="text-4xl mb-2">📦</div>
              <h3 className="font-semibold">{category}</h3>
            </Link>
          ))}
        </div>
      </section>

      {/* Doporučené produkty */}
      <section className="container mx-auto px-4 py-12">
        <div className="flex justify-between items-center mb-8">
          <h2 className="text-3xl font-bold">Doporučené produkty</h2>
          <Link to="/products" className="text-primary-600 hover:text-primary-700 font-semibold">
            Zobrazit všechny →
          </Link>
        </div>
        
        {searchTerm ? (
          <div>
            <h3 className="text-xl font-semibold mb-4">
              Výsledky vyhledávání ({filteredProducts.length})
            </h3>
            {filteredProducts.length > 0 ? (
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
                {filteredProducts.map((product) => (
                  <ProductCard key={product.id} product={product} />
                ))}
              </div>
            ) : (
              <p className="text-center text-gray-500 py-12">
                Žádné produkty nenalezeny
              </p>
            )}
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
            {featuredProducts.map((product) => (
              <ProductCard key={product.id} product={product} />
            ))}
          </div>
        )}
      </section>
    </div>
  );
};

export default HomePage;

