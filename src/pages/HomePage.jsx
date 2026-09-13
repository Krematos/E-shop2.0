import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getProducts } from '../services/productService';
import { useCart } from '../context/useCart';
import ProductCard from '../components/ProductCard';
import SkeletonCard from '../components/SkeletonCard';
import SEO from '../components/SEO';
import { Monitor, Shirt, Home, Trophy } from 'lucide-react';

const HomePage = () => {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [featuredProducts, setFeaturedProducts] = useState([]);
  const [fetchError, setFetchError] = useState('');
  const { getTotalItems } = useCart();

  useEffect(() => {
    const fetchProducts = async () => {
      try {
        setLoading(true);
        setFetchError('');
        const data = await getProducts();

        console.log('Načtené produkty:', data);

        // Spring Data REST vrací PagedModel: { _embedded: { products: [...] }, page: {...} }
        // Spring Page vrací: { content: [...], totalPages: ... }
        const productsList =
          data?._embedded?.homepageProductResponseList ??
          data?._embedded?.productCatalogResponseList ??
          data?._embedded?.productResponseList ??
          data?._embedded?.productList ??
          data?._embedded?.products ??
          data?.content ??
          (Array.isArray(data) ? data : []);

        if (Array.isArray(productsList)) {
          setProducts(productsList);
          setFeaturedProducts(productsList.slice(0, 6));
        } else {
          console.error('Neočekávaný formát dat:', data);
          setProducts([]);
          setFeaturedProducts([]);
        }
      } catch (error) {
        console.error('Chyba při načítání produktů:', error);
        setProducts([]); // I při chybě sítě nastavíme prázdné pole
        setFeaturedProducts([]);
        if (!error.response) {
          setFetchError('Nepodařilo se připojit k serveru. Zkontrolujte internetové připojení.');
        } else {
          setFetchError('Nepodařilo se načíst produkty. Zkuste to prosím později.');
        }
      } finally {
        setLoading(false);
      }
    };

    fetchProducts();
  }, []);

  const filteredProducts = products.filter((product) =>
    product?.name?.toLowerCase()?.includes(searchTerm.toLowerCase())
  );

  return (
    <div className="min-h-screen">
      <SEO
        title="SecondEl - Ověřená elektronika a technika se zárukou"
        description="Objevte široký výběr kvalitní a prověřené elektroniky na SecondEl. Bezpečné online platby, rychlá doprava a férové ceny se zárukou."
      />

      {/* Hero sekce */}
      <section className="bg-gradient-to-r from-primary-600 to-primary-800 text-white py-20">
        <div className="container mx-auto px-4 text-center">
          <h1 className="text-4xl md:text-5xl font-bold mb-4">
            Vítejte v našem E-Shopu
          </h1>
          <p className="text-xl mb-8 text-primary-100">
            Objevte široký výběr kvalitních produktů
          </p>

          {/* Vyhledávací lišta a košík */}
          <div className="max-w-3xl mx-auto flex flex-col sm:flex-row gap-4 items-center">
            <input
              type="text"
              placeholder="Hledat produkty..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full px-6 py-3 rounded-lg text-gray-900 focus:ring-2 focus:ring-white outline-none"
            />
            {/* Košík - přesunut na úroveň vyhledávání vpravo */}
            <Link to="/cart" aria-label="Přejít do košíku" className="relative p-2 text-white hover:text-primary-200 transition-colors flex items-center gap-2 group flex-shrink-0">
              <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.75} stroke="currentColor" className="w-9 h-9 group-hover:scale-105 transition-transform">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M2.25 3h1.386c.51 0 .955.343 1.087.835l.383 1.437M7.5 14.25a3 3 0 0 0-3 3h15.75m-12.75-3h11.218c1.121-2.3 2.1-4.684 2.924-7.138a60.114 60.114 0 0 0-16.536-1.84M7.5 14.25 5.106 5.272M6 20.25a.75.75 0 1 1-1.5 0 .75.75 0 0 1 1.5 0Zm12.75 0a.75.75 0 1 1-1.5 0 .75.75 0 0 1 1.5 0Z" />
              </svg>
              <span className="font-semibold text-lg hidden md:inline">Košík</span>
              {getTotalItems() > 0 && (
                <span className="absolute top-0 right-0 md:-right-2 bg-red-500 text-white text-xs font-bold rounded-full w-5 h-5 flex items-center justify-center shadow-sm">
                  {getTotalItems()}
                </span>
              )}
            </Link>
          </div>
        </div>
      </section>

      {/* Kategorie */}
      <section className="container mx-auto px-4 py-12">
        <h2 className="text-3xl font-bold mb-8 text-center">Kategorie</h2>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          {[
            { name: 'Elektronika', icon: Monitor },
            { name: 'Oblečení', icon: Shirt },
            { name: 'Domácnost', icon: Home },
            { name: 'Sport', icon: Trophy },
          ].map((category) => {
            const Icon = category.icon;
            return (
              <Link
                key={category.name}
                to={`/products?category=${category.name}`}
                className="card flex flex-col items-center justify-center text-center hover:shadow-lg transition-transform hover:-translate-y-1"
              >
                <div className="mb-3">
                  <Icon className="h-12 w-12 text-primary-600 stroke-1" />
                </div>
                <h3 className="font-semibold text-gray-800">{category.name}</h3>
              </Link>
            );
          })}
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

        {fetchError && (
          <div className="max-w-2xl mx-auto mb-8 p-4 bg-red-100 border border-red-300 text-red-700 rounded-md text-center">
            {fetchError}
          </div>
        )}

        {searchTerm ? (
          <div>
            <h3 className="text-xl font-semibold mb-4">
              Výsledky vyhledávání ({filteredProducts.length})
            </h3>
            {loading ? (
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
                {[...Array(4)].map((_, i) => <SkeletonCard key={i} />)}
              </div>
            ) : filteredProducts.length > 0 ? (
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
            {loading ? (
              [...Array(4)].map((_, i) => <SkeletonCard key={i} />)
            ) : (
              featuredProducts.map((product) => (
                <ProductCard key={product.id} product={product} />
              ))
            )}
          </div>
        )}
      </section>
    </div>
  );
};

export default HomePage;
