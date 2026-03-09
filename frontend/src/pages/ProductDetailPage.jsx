import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getProductById } from '../services/productService';
import { useCart } from '../context/CartContext';
import LoadingSpinner from '../components/LoadingSpinner';
import { getImageUrl } from '../utils/urlUtils';

const ProductDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { addToCart } = useCart();
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [quantity, setQuantity] = useState(1);

  useEffect(() => {
    const fetchProduct = async () => {
      try {
        const data = await getProductById(id);
        setProduct(data);
      } catch (error) {
        console.error('Chyba při načítání produktu:', error);
        navigate('/products');
      } finally {
        setLoading(false);
      }
    };

    fetchProduct();
  }, [id, navigate]);

  const handleAddToCart = () => {
    if (product) {
      addToCart(product, quantity);
      alert('Produkt byl přidán do košíku!');
    }
  };

  if (loading) {
    return <LoadingSpinner />;
  }

  if (!product) {
    return (
      <div className="container mx-auto px-4 py-8 text-center">
        <p className="text-gray-500">Produkt nenalezen</p>
      </div>
    );
  }

  const price = typeof product.price === 'string'
    ? Number.parseFloat(product.price)
    : product.price;

  return (
    <div className="container mx-auto px-4 py-8">
      <button
        onClick={() => navigate(-1)}
        className="text-primary-600 hover:text-primary-700 mb-4"
      >
        ← Zpět
      </button>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
        {/* Obrázek produktu */}
        <div className="aspect-square bg-gray-200 rounded-lg flex items-center justify-center overflow-hidden">
          {product.images?.[0] ? (
            <img
              src={getImageUrl(product.images[0])}
              alt={product.name}
              className="w-full h-full object-cover"
            />
          ) : (
            <div className="text-gray-400 text-8xl">📦</div>
          )}
        </div>

        {/* Informace o produktu */}
        <div>
          <h1 className="text-4xl font-bold mb-4">{product.name}</h1>
          <p className="text-3xl font-bold text-primary-600 mb-6">
            {price?.toFixed(2) || '0.00'} Kč
          </p>

          <div className="mb-6">
            <h2 className="text-xl font-semibold mb-2">Popis</h2>
            <p className="text-gray-700">{product.description || 'Žádný popis'}</p>
          </div>

          {/* Výběr množství */}
          <div className="mb-6">
            <label className="block text-sm font-semibold mb-2">Množství></label>
            <div className="flex items-center space-x-4">
              <button
                onClick={() => setQuantity(Math.max(1, quantity - 1))}
                className="bg-gray-200 hover:bg-gray-300 px-4 py-2 rounded-lg"
              >
                -
              </button>
              <span className="text-xl font-semibold w-12 text-center">{quantity}</span>
              <button
                onClick={() => setQuantity(quantity + 1)}
                className="bg-gray-200 hover:bg-gray-300 px-4 py-2 rounded-lg"
              >
                +
              </button>
            </div>
          </div>

          {/* Tlačítko přidat do košíku */}
          <button
            onClick={handleAddToCart}
            className="btn-primary w-full text-lg py-3"
          >
            Přidat do košíku
          </button>
        </div>
      </div>
    </div>
  );
};

export default ProductDetailPage;

