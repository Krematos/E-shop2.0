import { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { getProducts } from '../services/productService';
import ProductCard from '../components/ProductCard';
import LoadingSpinner from '../components/LoadingSpinner';

const ProductListPage = () => {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchParams] = useSearchParams();
  const category = searchParams.get('category');
  const [searchTerm, setSearchTerm] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  useEffect(() => {
    const fetchProducts = async () => {
      setLoading(true);
      try {
        const data = await getProducts(page);
        setProducts(data.content);
        setTotalPages(data.totalPages);
      } catch (error) {
        console.error('Chyba při načítání produktů:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchProducts();
  }, [page]);

  const filteredProducts = products.filter((product) => {
    const matchesSearch = product.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      product.description?.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesCategory = !category || product.category === category;
    return matchesSearch && matchesCategory;
  });

  if (loading) {
    return <LoadingSpinner />;
  }

  const Pagination = () => (
    <div className="flex justify-center items-center space-x-4 mt-8">
      <button
        onClick={() => setPage(page - 1)}
        disabled={page === 0}
        className="btn btn-primary"
      >
        Předchozí
      </button>
      <span>
        Stránka {page + 1} z {totalPages}
      </span>
      <button
        onClick={() => setPage(page + 1)}
        disabled={page >= totalPages - 1}
        className="btn btn-primary"
      >
        Následující
      </button>
    </div>
  );

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold mb-6">Seznam produktů</h1>

      {/* Vyhledávací a filtrační lišta */}
      <div className="mb-8">
        <input
          type="text"
          placeholder="Hledat produkty..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className="input-field max-w-md"
        />
        {category && (
          <div className="mt-4">
            <span className="text-gray-600">Filtr: </span>
            <span className="bg-primary-100 text-primary-800 px-3 py-1 rounded-full">
              {category}
            </span>
          </div>
        )}
      </div>

      {/* Seznam produktů */}
      {filteredProducts.length > 0 ? (
        <>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
            {filteredProducts.map((product) => (
              <ProductCard key={product.id} product={product} />
            ))}
          </div>
          <Pagination />
        </>
      ) : (
        <div className="text-center py-12">
          <p className="text-gray-500 text-lg">Žádné produkty nenalezeny</p>
        </div>
      )}
    </div>
  );
};

export default ProductListPage;

