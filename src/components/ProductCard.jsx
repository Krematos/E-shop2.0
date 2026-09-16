import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useCart } from '../context/useCart';
import { Package } from 'lucide-react';

import { getImageUrl, getResponsiveSrcSet, defaultImageSizes } from '../utils/urlUtils';

const ProductCard = ({ product }) => {

  console.log("Data produktu:", product?.name, product?.images);

  const { addToCart } = useCart();
  const [imgError, setImgError] = useState(false);

  const mainImageFilename = product?.mainImage || product?.mainImageUrl || product?.images?.find?.(img => img?.main || img?.isMain)?.url || product?.images?.[0]?.url || product?.images?.[0];

  // Aktuální stav skladu
  const currentStock = product?.availableStock === undefined ? product?.stockQuantity : product.availableStock;
  const isOutOfStock = currentStock === undefined || currentStock <= 0;

  // Sestavení URL obrázku
  const imageUrl = getImageUrl(mainImageFilename);

  const handleAddToCart = (e) => {
    e.preventDefault(); // Zabrání prokliku na detail produktu při kliknutí na tlačítko
    e.stopPropagation(); // Zastaví šíření události
    addToCart(product, 1);
  };

  const rawPrice = typeof product.price === 'string'
    ? Number.parseFloat(product.price)
    : product.price;

  const formattedPrice = new Intl.NumberFormat('cs-CZ', {
    style: 'currency',
    currency: 'CZK',
    minimumFractionDigits: 0,
    maximumFractionDigits: 2,
  }).format(rawPrice || 0);



  return (
    <div className="card group hover:shadow-xl transition-all duration-300 border border-gray-100 rounded-xl bg-white flex flex-col h-full">
      <Link to={`/products/${product.id}`} className="flex-1 flex flex-col">
        {/* Kontejner obrázku */}
        <div className="aspect-[4/3] bg-gray-50 rounded-t-xl overflow-hidden relative">
          {!imgError && mainImageFilename ? (
            <img
              src={imageUrl}
              srcSet={getResponsiveSrcSet(mainImageFilename)}
              sizes={defaultImageSizes}
              width="440"
              height="330"
              alt={product.name}
              loading="lazy" // Optimalizace výkonu
              onError={() => setImgError(true)} // Pokud server vrátí 404, přepne na fallback
              className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
            />
          ) : (
            // Fallback ikona, pokud obrázek chybí nebo se nenačetl
            <div className="w-full h-full flex items-center justify-center bg-gray-100 text-gray-400">
              <Package size={48} className="stroke-1" />
            </div>
          )}
        </div>

        {/* Obsah karty */}
        <div className="p-4 flex flex-col flex-1">
          <h3 className="text-lg font-bold text-gray-800 mb-2 line-clamp-2 leading-tight group-hover:text-primary-600 transition-colors">
            {product.name}
          </h3>

          <p className="text-gray-500 text-sm mb-4 line-clamp-3 flex-1">
            {product.description || "Bez popisu"}
          </p>

          <div className="mt-auto pt-4 border-t border-gray-100 flex items-baseline justify-between gap-2">
            <div>
              <span className="text-xl font-bold text-primary-600">
                {formattedPrice}
              </span>
              <span className="text-xs text-gray-400 font-normal ml-1.5">včetně DPH</span>
            </div>
          </div>
        </div>
      </Link>

      {/* Tlačítko je oddělené, ale vizuálně v kartě */}
      <div className="p-4 pt-0">
        <button
          onClick={handleAddToCart}
          disabled={isOutOfStock}
          className={`w-full py-2.5 rounded-lg font-medium shadow-sm transition-all ${
            isOutOfStock
              ? 'bg-gray-100 text-gray-400 cursor-not-allowed border border-gray-200'
              : 'btn-primary hover:shadow-md active:scale-95'
          }`}
        >
          {isOutOfStock ? 'Vyprodáno' : 'Přidat do košíku'}
        </button>
      </div>
    </div>
  );
};

export default ProductCard;
