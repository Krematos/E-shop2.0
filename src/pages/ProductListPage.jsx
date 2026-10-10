import { useState, useEffect } from 'react';
import { useSearchParams, useParams, Link } from 'react-router-dom';
import { getProducts } from '../services/productService';
import { useCart } from '../context/useCart';
import { slugify } from '../utils/urlUtils';
import ProductCard from '../components/ProductCard';
import SkeletonCard from '../components/SkeletonCard';
import SEO from '../components/SEO';

const CATEGORY_MAP = {
  'elektronika': { title: 'Elektronika', dbNames: ['ELEKTRONIKA', 'Elektronika'] },
  'prislusenstvi': { title: 'Příslušenství', dbNames: ['PRISLUSENSTVI', 'Příslušenství'] },
  'pocitace-a-notebooky': { title: 'Počítače a notebooky', dbNames: ['ELEKTRONIKA', 'Elektronika', 'POCITACE', 'Notebooky'] },
  'chytre-telefony': { title: 'Chytré telefony', dbNames: ['ELEKTRONIKA', 'Elektronika', 'TELEFONY', 'Smartphony'] },
  'audio-a-sluchatka': { title: 'Audio a sluchátka', dbNames: ['ELEKTRONIKA', 'Elektronika', 'AUDIO', 'Sluchátka'] }
};

const ProductListPage = () => {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const { getTotalItems } = useCart();
  const [searchParams] = useSearchParams();
  const { categorySlug } = useParams();

  const queryCategory = searchParams.get('category');
  const activeCategorySlug = categorySlug || (queryCategory ? slugify(queryCategory) : null);
  const activeCategoryTitle = categorySlug
    ? (CATEGORY_MAP[categorySlug]?.title || categorySlug.replace(/-/g, ' '))
    : queryCategory;

  const [searchTerm, setSearchTerm] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [fetchError, setFetchError] = useState('');

  useEffect(() => {
    const fetchProducts = async () => {
      setLoading(true);
      setFetchError('');
      try {
        const data = await getProducts(page);
        // Spring Data REST vrací PagedModel: { _embedded: { products: [...] }, page: { totalPages, totalElements, ... } }
        // Spring Page vrací: { content: [...], totalPages: ... }
        const productsList =
          data?._embedded?.homepageProductResponseList ??
          data?._embedded?.productCatalogResponseList ??
          data?._embedded?.productResponseList ??
          data?._embedded?.productResponses ??
          data?._embedded?.productList ??
          data?._embedded?.products ??
          data?.content ??
          (Array.isArray(data) ? data : []);
        setProducts(Array.isArray(productsList) ? productsList : []);
        setTotalPages(data?.page?.totalPages ?? data?.totalPages ?? 0);
      } catch (error) {
        console.error('Chyba při načítání produktů:', error);
        setProducts([]);
        if (!error.response) {
          setFetchError('Nepodařilo se připojit k serveru. Zkontrolujte připojení k internetu.');
        } else {
          setFetchError('Nepodařilo se načíst detail produktů. Zkuste to prosím později.');
        }
      } finally {
        setLoading(false);
      }
    };

    fetchProducts();
  }, [page]);

  const filteredProducts = products.filter((product) => {
    const matchesSearch = product.name?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      product.description?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      false;

    let matchesCategory = true;
    if (activeCategorySlug) {
      const mapped = CATEGORY_MAP[activeCategorySlug];
      if (mapped) {
        matchesCategory = mapped.dbNames.some(
          db => db.toLowerCase() === product.category?.toLowerCase()
        );
      } else {
        matchesCategory = slugify(product.category) === activeCategorySlug ||
          product.category?.toLowerCase() === activeCategorySlug.toLowerCase();
      }
    } else if (queryCategory) {
      matchesCategory = product.category?.toLowerCase() === queryCategory.toLowerCase();
    }

    return matchesSearch && matchesCategory;
  });

  const pageTitle = activeCategoryTitle
    ? `${activeCategoryTitle} | SecondEl`
    : 'Katalog prověřené elektroniky';
  const pageDescription = activeCategoryTitle
    ? `Prohlédněte si naši nabídku v kategorii ${activeCategoryTitle} na SecondEl. Skvělé ceny prověřené elektroniky, rychlé dodání a záruka.`
    : 'Kompletní katalog prověřené elektroniky a příslušenství se zárukou na SecondEl. Vyberte si z naší nabídky otestovaného zboží.';

  // Canonical URL přesně respektuje zvolenou kategorii, aby se neslévala na obecné /products
  const canonicalUrl = activeCategorySlug
    ? `https://secondel.cz/kategorie/${activeCategorySlug}`
    : 'https://secondel.cz/products';

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
      <SEO
        title={pageTitle}
        description={pageDescription}
        canonical={canonicalUrl}
      />

      {/* Crawlovatelná drobečková navigace */}
      <nav className="text-sm text-gray-500 mb-6" aria-label="Breadcrumb">
        <ol className="flex items-center space-x-2">
          <li><Link to="/" className="hover:text-primary-600">Domů</Link></li>
          <li><span>/</span></li>
          {activeCategoryTitle ? (
            <>
              <li><Link to="/products" className="hover:text-primary-600">Produkty</Link></li>
              <li><span>/</span></li>
              <li className="text-gray-900 font-medium">{activeCategoryTitle}</li>
            </>
          ) : (
            <li className="text-gray-900 font-medium">Produkty</li>
          )}
        </ol>
      </nav>

      <h1 className="text-3xl font-bold mb-6">
        {activeCategoryTitle ? `Produkty v kategorii ${activeCategoryTitle}` : 'Seznam produktů'}
      </h1>

      {/* Vyhledávací a filtrační lišta včetně Košíku */}
      <div className="mb-8 flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div className="w-full sm:w-auto">
          <input
            type="text"
            placeholder="Hledat produkty..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="input-field w-full max-w-md"
          />
          {activeCategoryTitle && (
            <div className="mt-4 flex items-center gap-2">
              <span className="text-gray-600">Filtr: </span>
              <span className="bg-primary-100 text-primary-800 px-3 py-1 rounded-full text-sm font-medium">
                {activeCategoryTitle}
              </span>
              <Link
                to="/products"
                className="text-xs text-gray-500 hover:text-red-600 underline ml-2"
              >
                Zrušit filtr
              </Link>
            </div>
          )}
        </div>

        {/* Košík - přesunut na úroveň vyhledávání vpravo */}
        <div className="flex items-center self-end sm:self-auto">
          <Link to="/cart" aria-label="Přejít do košíku" className="relative p-2 text-gray-700 hover:text-primary-600 transition-colors flex items-center gap-2 group">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.75} stroke="currentColor" className="w-8 h-8 group-hover:scale-105 transition-transform">
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

      {fetchError && (
        <div className="max-w-4xl mx-auto mb-8 p-4 bg-red-100 border border-red-300 text-red-700 rounded-md text-center">
          {fetchError}
        </div>
      )}

      {/* Seznam produktů nebo Skeletons */}
      {loading ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          {[...Array(8)].map((_, index) => (
            <SkeletonCard key={index} />
          ))}
        </div>
      ) : filteredProducts.length > 0 ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          {filteredProducts.map((product) => (
            <ProductCard key={product.id} product={product} />
          ))}
        </div>
      ) : (
        <div className="text-center py-12">
          <p className="text-gray-500 text-lg">Žádné produkty nenalezeny</p>
          <Link
            to="/products"
            className="text-primary-600 hover:text-primary-700 mt-2 inline-block font-semibold"
          >
            Zobrazit všechny produkty
          </Link>
        </div>
      )}

      {totalPages > 1 && <Pagination />}
    </div>
  );
};

export default ProductListPage;
