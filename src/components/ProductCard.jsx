import { Link } from 'react-router-dom';
import { useCart } from '../context/CartContext';

const ProductCard = ({ product }) => {
  const { addToCart } = useCart();

  const handleAddToCart = (e) => {
    e.preventDefault();
    addToCart(product, 1);
  };

  const price = typeof product.price === 'string' 
    ? parseFloat(product.price) 
    : product.price;

  return (
    <div className="card hover:shadow-lg transition-shadow duration-200">
      <Link to={`/products/${product.id}`}>
        <div className="aspect-square bg-gray-200 rounded-lg mb-4 flex items-center justify-center overflow-hidden">
          {product.image ? (
            <img
              src={product.image}
              alt={product.name}
              className="w-full h-full object-cover"
            />
          ) : (
            <div className="text-gray-400 text-4xl">📦</div>
          )}
        </div>
        <h3 className="text-xl font-semibold mb-2 line-clamp-2">{product.name}</h3>
        <p className="text-gray-600 text-sm mb-4 line-clamp-2">
          {product.description}
        </p>
        <div className="flex items-center justify-between">
          <span className="text-2xl font-bold text-primary-600">
            {price?.toFixed(2) || '0.00'} Kč
          </span>
        </div>
      </Link>
      <button
        onClick={handleAddToCart}
        className="btn-primary w-full mt-4"
      >
        Přidat do košíku
      </button>
    </div>
  );
};

export default ProductCard;
