import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getProductById } from '../services/productService';
import { useCart } from '../context/useCart';
import { getImageUrl } from '../utils/urlUtils';
import LoadingSpinner from '../components/LoadingSpinner';
import SEO from '../components/SEO';

const ProductDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { addToCart } = useCart();

  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [fetchError, setFetchError] = useState('');
  const [currentImageIndex, setCurrentImageIndex] = useState(0);
  const [showSuccess, setShowSuccess] = useState(false);

  useEffect(() => {
    const fetchProduct = async () => {
      try {
        setFetchError('');
        const data = await getProductById(id);
        setProduct(data);
        
        // Find the main image index, default to 0 if not found
        let mainIdx = 0;
        if (data.images && Array.isArray(data.images)) {
          const foundIdx = data.images.findIndex(img => img.main || img.isMain);
          if (foundIdx !== -1) {
            mainIdx = foundIdx;
          }
        }
        setCurrentImageIndex(mainIdx);
      } catch (error) {
        console.error('Chyba při načítání produktu:', error);
        if (!error.response) {
          setFetchError('Nepodařilo se připojit k serveru. Zkontrolujte připojení k internetu.');
        } else if (error.response.status === 404) {
          setFetchError('Produkt nebyl nalezen. Pravděpodobně byl smazán nebo adresa neexistuje.');
        } else {
          setFetchError('Nepodařilo se načíst detail produktu. Zkuste to prosím později.');
        }
      } finally {
        setLoading(false);
      }
    };

    fetchProduct();
  }, [id, navigate]);

  const handleAddToCart = () => {
    if (product) {
      addToCart(product, 1);
      setShowSuccess(true);
      setTimeout(() => setShowSuccess(false), 3000);
    }
  };

  if (loading) {
    return <LoadingSpinner />;
  }

  if (fetchError) {
    return (
      <div className="container mx-auto px-4 py-8 mt-8">
        <div role="alert" className="max-w-2xl mx-auto p-4 bg-red-100 border border-red-300 text-red-700 rounded-md text-center">
          {fetchError}
        </div>
        <div className="text-center mt-4">
          <button onClick={() => navigate('/products')} className="text-primary-600 hover:text-primary-700 font-semibold">
            ← Zpět na seznam produktů
          </button>
        </div>
      </div>
    );
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

  // SEO metadata a Open Graph obrázek
  const currentImageUrl = product.images?.[currentImageIndex]?.url
    ? getImageUrl(product.images[currentImageIndex].url)
    : (product.images?.[0]?.url ? getImageUrl(product.images[0].url) : undefined);

  const productDescription = product.description
    ? (product.description.length > 155 ? `${product.description.substring(0, 152)}...` : product.description)
    : `Kupte ${product.name} za skvělou cenu na e-shopu SecondEl. Rychlé doručení a prověřená kvalita.`;

  // Schema.org strukturovaná data pro Google Rich Snippets
  const productSchema = {
    '@context': 'https://schema.org',
    '@type': 'Product',
    name: product.name,
    description: product.description || product.name,
    image: currentImageUrl,
    category: product.category,
    offers: {
      '@type': 'Offer',
      price: price,
      priceCurrency: 'CZK',
      availability: product.stockQuantity > 0 ? 'https://schema.org/InStock' : 'https://schema.org/OutOfStock',
      url: typeof window !== 'undefined' ? window.location.href : undefined
    }
  };

  return (
    <div className="container mx-auto px-4 py-8">
      <SEO
        title={`${product.name} - Koupit`}
        description={productDescription}
        image={currentImageUrl}
        type="product"
        schema={productSchema}
      />
      
      {/* Breadcrumb navigation */}
      <nav className="text-sm text-gray-500 mb-6" aria-label="Breadcrumb">
        <ol className="flex items-center space-x-2">
          <li><button onClick={() => navigate('/')} className="hover:text-primary-600">Domů</button></li>
          <li><span>/</span></li>
          <li><button onClick={() => navigate('/products')} className="hover:text-primary-600">Produkty</button></li>
          <li><span>/</span></li>
          <li className="text-gray-900 font-medium truncate max-w-xs">{product.name}</li>
        </ol>
      </nav>

      {showSuccess && (
        <div className="mb-6 p-4 bg-green-50 border border-green-200 text-green-700 rounded-lg flex items-center justify-between animate-fade-in shadow-sm">
          <div className="flex items-center space-x-2">
            <span className="text-xl">✓</span>
            <span>Produkt byl úspěšně přidán do košíku!</span>
          </div>
          <button 
            onClick={() => navigate('/cart')} 
            className="text-sm font-semibold underline hover:text-green-800 ml-4"
          >
            Přejít do košíku
          </button>
        </div>
      )}

      <div className="grid grid-cols-1 md:grid-cols-2 gap-8 lg:gap-12">
        {/* Left column: Images gallery */}
        <div className="space-y-4">
          <div className="aspect-square bg-gray-100 rounded-2xl overflow-hidden border border-gray-100 flex items-center justify-center p-4">
            {product.images && product.images.length > 0 ? (
              <img
                src={getImageUrl(product.images[currentImageIndex]?.url)}
                alt={`${product.name} - pohled ${currentImageIndex + 1}`}
                className="w-full h-full object-contain max-h-[500px]"
              />
            ) : (
              <div className="text-gray-400 flex flex-col items-center">
                <span className="text-4xl mb-2">📷</span>
                <span>Bez obrázku</span>
              </div>
            )}
          </div>

          {/* Thumbnails list */}
          {product.images && product.images.length > 1 && (
            <div className="flex space-x-3 overflow-x-auto pb-2">
              {product.images.map((img, idx) => (
                <button
                  key={img.id || idx}
                  onClick={() => setCurrentImageIndex(idx)}
                  className={`w-20 h-20 rounded-lg overflow-hidden border-2 flex-shrink-0 bg-gray-50 transition-all ${
                    currentImageIndex === idx ? 'border-primary-600 scale-105 shadow-sm' : 'border-gray-200 hover:border-gray-300'
                  }`}
                  aria-label={`Zobrazit obrázek ${idx + 1}`}
                >
                  <img
                    src={getImageUrl(img.url)}
                    alt=""
                    className="w-full h-full object-cover"
                  />
                </button>
              ))}
            </div>
          )}
        </div>

        {/* Right column: Details and Action */}
        <div className="flex flex-col justify-between">
          <div className="space-y-6">
            <div>
              {product.category && (
                <span className="inline-block bg-primary-50 text-primary-700 text-xs px-2.5 py-1 rounded-full font-medium mb-3 uppercase tracking-wider">
                  {product.category}
                </span>
              )}
              <h1 className="text-3xl lg:text-4xl font-extrabold text-gray-900 tracking-tight leading-tight">
                {product.name}
              </h1>
            </div>

            <div className="border-t border-b border-gray-100 py-4 flex items-baseline space-x-4">
              <p className="text-3xl font-extrabold text-primary-600">
                {price.toFixed(2)} Kč
              </p>
              <span className="text-sm text-gray-400">včetně DPH</span>
            </div>

            <div className="space-y-2">
              <label className="text-sm font-semibold text-gray-700 uppercase tracking-wider block">
                Dostupnost
              </label>
              <div>
                {product.stockQuantity > 0 ? (
                  <div className="flex items-center text-green-600 font-medium">
                    <span className="inline-block w-2.5 h-2.5 rounded-full bg-green-500 mr-2"></span>
                    Skladem ({product.stockQuantity} ks)
                  </div>
                ) : (
                  <div className="flex items-center text-red-600 font-medium">
                    <span className="inline-block w-2.5 h-2.5 rounded-full bg-red-500 mr-2"></span>
                    Vyprodáno
                  </div>
                )}
              </div>
            </div>

            <div className="space-y-2">
              <label className="text-sm font-semibold text-gray-700 uppercase tracking-wider block">
                Popis produktu
              </label>
              <div className="prose prose-sm text-gray-600 leading-relaxed max-w-none">
                <p>{product.description || 'K tomuto produktu zatím není k dispozici žádný detailní popis.'}</p>
              </div>
            </div>
          </div>

          <div className="mt-8 pt-6 border-t border-gray-100 space-y-4">
            <button
              onClick={handleAddToCart}
              disabled={product.stockQuantity <= 0}
              className={`w-full py-4 px-6 rounded-xl font-bold text-lg flex items-center justify-center space-x-2 transition-all shadow-md ${
                product.stockQuantity > 0
                  ? 'btn-primary hover:shadow-lg active:scale-[0.99]'
                  : 'bg-gray-200 text-gray-400 cursor-not-allowed border border-gray-300'
              }`}
            >
              {product.stockQuantity > 0 ? (
                <>
                  <span>🛒</span>
                  <span>Přidat do košíku</span>
                </>
              ) : (
                <span>Vyprodáno</span>
              )}
            </button>
            <button
              onClick={() => navigate('/products')}
              className="w-full text-center text-sm text-gray-500 hover:text-gray-800 font-medium py-2"
            >
              ← Zpět na výběr produktů
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ProductDetailPage;
