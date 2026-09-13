import { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import ProtectedRoute from '../components/ProtectedRoute';
import {
  getProducts,
  getProductById,
  deleteProduct,
} from '../services/productService';
import LoadingSpinner from '../components/LoadingSpinner';
import AdminAddProductForm from '../components/AdminAddProductForm';

const AdminPage = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('products');
  const [showProductForm, setShowProductForm] = useState(false);
  const [editingProduct, setEditingProduct] = useState(null);
  const [productPage, setProductPage] = useState(0);
  const [productTotalPages, setProductTotalPages] = useState(0);
  const [productFilter, setProductFilter] = useState('active'); // 'active', 'archived'
  const [fetchError, setFetchError] = useState('');
  const [actionError, setActionError] = useState('');
  const [actionSuccess, setActionSuccess] = useState('');

  useEffect(() => {
    fetchData(productPage);
  }, [productPage]);

  // Poslouchání state.action z react-router-dom (např. kliknutí na "Přidat produkt" v menu)
  useEffect(() => {
    if (location.state?.action === 'add-product') {
      setActiveTab('products');
      setShowProductForm(true);
      setEditingProduct(null);
      setActionError('');
      setActionSuccess('');
      // Vyčistit state historie, aby po refreshi form nezůstal otevřený
      globalThis.history.replaceState({}, document.title);
    }
  }, [location.state]);

  const fetchData = async (page = 0) => {
    setLoading(true);
    try {
      const productsData = await getProducts(page, 6);
      const productsList =
        productsData?._embedded?.homepageProductResponseList ??
        productsData?._embedded?.productCatalogResponseList ??
        productsData?._embedded?.productResponseList ??
        productsData?._embedded?.productResponses ??
        productsData?._embedded?.productList ??
        productsData?._embedded?.products ??
        productsData?.content ??
        [];
      
      setProducts(Array.isArray(productsList) ? productsList : []);
      setProductTotalPages(productsData?.page?.totalPages ?? productsData?.totalPages ?? 0);
      } catch (error) {
      console.error('Chyba při načítání dat:', error);
      setProducts([]);
      if (error.response) {
        setFetchError('Nelze se spojit se serverem pro načtení dat. Zkontrolujte prosím své připojení k internetu.');
      } else {
        setFetchError(`Nastala chyba při získávání dat z administrace (Kód: ${error.response.status}). Zkuste stránku obnovit.`);
      }
    } finally {
      setLoading(false);
    }
  };



  const handleEditProduct = async (product) => {
    try {
      setLoading(true);
      const fullProduct = await getProductById(product.id);
      setEditingProduct(fullProduct);
      setShowProductForm(true);
    } catch (error) {
      console.error('Chyba při načítání detailu produktu:', error);
      const msg = error.response?.data?.message || 'Produkt se nepodařilo načíst z databáze.';
      setActionError(`Nepodařilo se načíst detail produktu pro úpravu: ${msg}`);
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteProduct = async (id) => {
    setActionError('');
    setActionSuccess('');
    if (!globalThis.confirm('Opravdu chcete archivovat tento produkt?')) {
      return;
    }

    try {
      await deleteProduct(id);
      setActionSuccess('Produkt byl archivován.');
      fetchData();
    } catch (error) {
      console.error('Chyba při archivaci produktu:', error);
      const msg = error.response?.data?.message || error.response?.data?.error || 'Produkt se nepodařilo archivovat.';
      setActionError(`Archivace selhala: ${msg}`);
    }
  };

  if (loading) {
    return <LoadingSpinner />;
  }

  return (
    <ProtectedRoute requireAdmin={true}>
      <div className="container mx-auto px-4 py-8">
        <h1 className="text-3xl font-bold mb-6">Administrace</h1>

        {/* Tabs */}
        <div className="border-b mb-6">
          <button
            onClick={() => setActiveTab('products')}
            className={`px-4 py-2 font-semibold ${
              activeTab === 'products'
                ? 'border-b-2 border-primary-600 text-primary-600'
                : 'text-gray-600'
            }`}
          >
            Správa produktů
          </button>
          <button
            onClick={() => navigate('/admin/orders')}
            className={`px-4 py-2 font-semibold text-gray-600`}
          >
            Objednávky
          </button>
        </div>

        {/* Products Tab */}
        {activeTab === 'products' && (
          <div>
            <div className="flex justify-between items-center mb-4">
              <h2 className="text-2xl font-semibold">Produkty</h2>
              <button
                onClick={() => {
                  setActionError('');
                  setActionSuccess('');
                  setEditingProduct(null);
                  setShowProductForm(true);
                }}
                className="btn-primary"
              >
                Přidat produkt
              </button>
            </div>

            {fetchError && (
              <div className="mb-4 p-4 bg-red-100 text-red-700 rounded-md border border-red-300">
                {fetchError}
              </div>
            )}
            {actionError && (
              <div className="mb-4 p-4 bg-red-100 text-red-700 rounded-md border border-red-300">
                {actionError}
              </div>
            )}
            {actionSuccess && (
              <div className="mb-4 p-4 bg-green-100 text-green-700 rounded-md border border-green-300">
                {actionSuccess}
              </div>
            )}

            {showProductForm && (
              <div className="card mb-6">
                <AdminAddProductForm
                                    initialData={editingProduct}
                                    onProductSaved={() => {
                                        setShowProductForm(false);
                                        setEditingProduct(null);
                                        fetchData(); // Znovu načte data po uložení
                                    }}
                                    onCancel={() => {
                                        setShowProductForm(false);
                                        setEditingProduct(null);
                                    }}
                                />
              </div>
            )}

            <div className="flex space-x-6 mb-4 border-b">
              <button
                onClick={() => setProductFilter('active')}
                className={`py-2 px-1 font-medium ${productFilter === 'active' ? 'border-b-2 border-primary-600 text-primary-600' : 'text-gray-500 hover:text-gray-700'}`}
              >
                Aktivní produkty
              </button>
              <button
                onClick={() => setProductFilter('archived')}
                className={`py-2 px-1 font-medium ${productFilter === 'archived' ? 'border-b-2 border-primary-600 text-primary-600' : 'text-gray-500 hover:text-gray-700'}`}
              >
                Archivované
              </button>
            </div>

            <div className="overflow-x-auto bg-white rounded-lg shadow">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Název
                    </th>
                    <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Cena
                    </th>
                    <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Status
                    </th>
                    <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Detail auditu
                    </th>
                    <th scope="col" className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Akce
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {products
                    .filter((product) => {
                      const isArchived = product.deletedAt != null;
                      if (productFilter === 'active') return !isArchived;
                      if (productFilter === 'archived') return isArchived;
                      return true;
                    })
                    .map((product) => {
                      const price = typeof product.price === 'string'
                        ? Number.parseFloat(product.price)
                        : product.price;
                      const isArchived = product.deletedAt != null;

                      const formattedDate = product.deletedAt 
                        ? new Date(product.deletedAt).toLocaleDateString('cs-CZ') 
                        : '';

                      return (
                        <tr key={product.id} className="hover:bg-gray-50">
                          <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                            {product.name}
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm font-bold text-primary-600">
                            {price?.toFixed(2) || '0.00'} Kč
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm">
                            {isArchived ? (
                              <span className="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-red-100 text-red-800 border border-red-200">
                                Archivováno
                              </span>
                            ) : (
                              <span className="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-green-100 text-green-800 border border-green-200">
                                Aktivní
                              </span>
                            )}
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                            {isArchived ? (
                              <span>
                                Smazáno uživatelem <span className="font-semibold">{product.deletedBy || 'Neznámý'}</span> dne {formattedDate}
                              </span>
                            ) : (
                              <span>-</span>
                            )}
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium space-x-2">
                            <button
                              onClick={() => handleEditProduct(product)}
                              className="text-primary-600 hover:text-primary-900 px-3 py-1 bg-primary-50 rounded"
                            >
                              Upravit
                            </button>
                            <button
                              onClick={() => handleDeleteProduct(product.id)}
                              disabled={isArchived}
                              className={`px-3 py-1 rounded ${isArchived ? 'text-gray-400 bg-gray-100 cursor-not-allowed' : 'text-red-600 hover:text-red-900 bg-red-50'}`}
                            >
                              {isArchived ? 'Archivováno' : 'Archivovat'}
                            </button>
                          </td>
                        </tr>
                      );
                    })}
                </tbody>
              </table>
              {products.length === 0 && (
                <div className="text-center py-8 text-gray-500">
                  Žádné produkty k zobrazení.
                </div>
              )}
            </div>
             {/* Pagination for products */}
             <div className="flex justify-center items-center mt-6">
              <button
                onClick={() => setProductPage((prev) => Math.max(prev - 1, 0))}
                disabled={productPage === 0}
                className="btn-secondary"
              >
                Předchozí
              </button>
              <span className="mx-4">
                Stránka {productPage + 1} z {productTotalPages}
              </span>
              <button
                onClick={() => setProductPage((prev) => Math.min(prev + 1, productTotalPages - 1))}
                disabled={productPage + 1 >= productTotalPages}
                className="btn-secondary"
              >
                Další
              </button>
            </div>
          </div>
        )}


      </div>
    </ProtectedRoute>
  );
};

export default AdminPage;

