import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getProductById } from '../services/productService';
import { useCart } from '../context/useCart';
import LoadingSpinner from '../components/LoadingSpinner';
import { getImageUrl, getResponsiveSrcSet } from '../utils/urlUtils';
import SEO from '../components/SEO';
import { Package } from 'lucide-react';

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
        <div className="max-w-2xl mx-auto p-4 bg-red-100 border border-red-300 text-red-700 rounded-md text-center">
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

      <button
        onClick={() => navigate(-1)}
        className="text-primary-600 hover:text-primary-700 mb-4"
      >
        ← Zpět
      </button>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
        {/* Obrázky produktu */}
        <div className="flex flex-col gap-4">
          <div className="aspect-[4/3] bg-gray-200 rounded-lg relative flex items-center justify-center overflow-hidden">
            {product.images?.length > 0 ? (
              <>
                <img
                  src={getImageUrl(product.images[currentImageIndex]?.url)}
                  srcSet={getResponsiveSrcSet(product.images[currentImageIndex]?.url)}
                  sizes="(max-width: 768px) 100vw, 50vw"
                  width="440"
                  height="330"
                  alt={product.name}
                  className="w-full h-full object-cover transition-opacity duration-300"
                />
                
                {/* Navigace pro více obrázků */}
                {product.images.length > 1 && (
                  <>
                    <button
                      onClick={() => setCurrentImageIndex((prev) => (prev === 0 ? product.images.length - 1 : prev - 1))}
                      className="absolute left-4 top-1/2 -translate-y-1/2 bg-white/90 hover:bg-white text-gray-800 w-10 h-10 rounded-full flex items-center justify-center shadow-lg transition-transform hover:scale-110 z-10 focus:outline-none"
                    >
                      ❮
                    </button>
                    <button
                      onClick={() => setCurrentImageIndex((prev) => (prev === product.images.length - 1 ? 0 : prev + 1))}
                      className="absolute right-4 top-1/2 -translate-y-1/2 bg-white/90 hover:bg-white text-gray-800 w-10 h-10 rounded-full flex items-center justify-center shadow-lg transition-transform hover:scale-110 z-10 focus:outline-none"
                    >
                      ❯
                    </button>
                  </>
                )}
              </>
            ) : (
              <div className="text-gray-300 flex items-center justify-center">
                <Package size={96} className="stroke-1" />
              </div>
            )}
          </div>
          
          {/* Náhledy obrázků (Thumbnails) */}
          {product.images?.length > 1 && (
            <div className="flex gap-2 overflow-x-auto py-1">
              {product.images.map((imgObj, index) => (
                <button
                  key={index}
                  onClick={() => setCurrentImageIndex(index)}
                  className={`relative flex-shrink-0 w-20 h-20 rounded-md overflow-hidden border-2 transition-all focus:outline-none ${
                    currentImageIndex === index 
                      ? 'border-primary-600 shadow-sm opacity-100' 
                      : 'border-transparent opacity-60 hover:opacity-100 hover:border-gray-300'
                  }`}
                >
                  <img
                    src={getImageUrl(imgObj.url)}
                    srcSet={getResponsiveSrcSet(imgObj.url)}
                    sizes="80px"
                    width="80"
                    height="80"
                    alt={`${product.name} - náhled ${index + 1}`}
                    className="w-full h-full object-cover"
                  />
                </button>
              ))}
            </div>
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

          {/* Zobrazení množství na skladě */}
          <div className="mb-6">
            <label className="block text-sm font-semibold mb-2">Skladem</label>
            <div className={`text-xl font-medium ${product.stockQuantity > 0 ? 'text-green-600' : 'text-red-600'}`}>
              {product.stockQuantity > 0 ? `${product.stockQuantity} ks` : 'Vyprodáno'}
            </div>
          </div>

          {/* Tlačítko přidat do košíku */}
          <button
            onClick={handleAddToCart}
            disabled={showSuccess || !product.stockQuantity || product.stockQuantity <= 0}
            className={`w-full text-lg py-3 rounded-md font-semibold transition-colors duration-200 ${
              !product.stockQuantity || product.stockQuantity <= 0
                ? 'bg-gray-100 text-gray-400 cursor-not-allowed border border-gray-200'
                : showSuccess 
                  ? 'bg-green-600 text-white cursor-default' 
                  : 'btn-primary'
            }`}
          >
            {!product.stockQuantity || product.stockQuantity <= 0 
              ? 'Vyprodáno' 
              : showSuccess 
                ? '✓ Přidáno do košíku' 
                : 'Přidat do košíku'}
          </button>

          {/* Oznámení o úspěchu */}
          {showSuccess && (
            <div className="mt-4 p-3 bg-green-50 text-green-700 border border-green-200 rounded-lg text-center font-medium shadow-sm transition-opacity duration-300">
              Můžete pokračovat do košíku nebo vybírat dál.
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default ProductDetailPage;
