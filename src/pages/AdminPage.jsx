import { useState, useEffect } from 'react';
import ProtectedRoute from '../components/ProtectedRoute';
import {
  getProducts,
  createProduct,
  updateProduct,
  deleteProduct,
} from '../services/productService';
import { getAllOrders } from '../services/orderService';
import LoadingSpinner from '../components/LoadingSpinner';
import AdminAddProductForm from '../components/AdminAddProductForm';

const AdminPage = () => {
  const [products, setProducts] = useState([]);
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('products');
  const [showProductForm, setShowProductForm] = useState(false);
  const [editingProduct, setEditingProduct] = useState(null);
  const [productPage, setProductPage] = useState(0);
  const [productTotalPages, setProductTotalPages] = useState(0);
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    price: '',
  });

  useEffect(() => {
    fetchData(productPage);
  }, [productPage]);

  const fetchData = async (page = 0) => {
    setLoading(true);
    try {
      const productsData = await getProducts(page, 6);
      if (productsData && productsData.content) {
        setProducts(productsData.content);
        setProductTotalPages(productsData.totalPages);
      } else {
        setProducts([]);
      }

      // Fetch orders only if the tab is active or initially
      if (activeTab === 'orders') {
        const ordersData = await getAllOrders();
        if (ordersData && ordersData.content) {
          setOrders(ordersData.content);
        } else {
          setOrders([]);
        }
      }
    } catch (error) {
      console.error('Chyba při načítání dat:', error);
      setProducts([]);
      setOrders([]);
    } finally {
      setLoading(false);
    }
  };

  const handleProductSubmit = async (e) => {
    e.preventDefault();
    try {
      const productData = {
        name: formData.name,
        description: formData.description,
        price: parseFloat(formData.price),
      };
        console.log('Odesílaná data produktu:', productData);
      if (editingProduct) {
        await updateProduct(editingProduct.id, productData);
      } else {
        await createProduct(productData);
      }

      setShowProductForm(false);
      setEditingProduct(null);
      setFormData({ name: '', description: '', price: '' });
      fetchData();
    } catch (error) {
      console.error('Chyba při ukládání produktu:', error);
      alert('Chyba při ukládání produktu');
    }
  };

  const handleEditProduct = (product) => {
    setEditingProduct(product);
    setFormData({
      name: product.name,
      description: product.description || '',
      price: product.price?.toString() || '',
    });
    setShowProductForm(true);
  };

  const handleDeleteProduct = async (id) => {
    if (!window.confirm('Opravdu chcete smazat tento produkt?')) {
      return;
    }

    try {
      await deleteProduct(id);
      fetchData();
    } catch (error) {
      console.error('Chyba při mazání produktu:', error);
      alert('Chyba při mazání produktu');
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('cs-CZ', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
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
            onClick={() => setActiveTab('orders')}
            className={`px-4 py-2 font-semibold ${
              activeTab === 'orders'
                ? 'border-b-2 border-primary-600 text-primary-600'
                : 'text-gray-600'
            }`}
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
                  setEditingProduct(null);
                  setFormData({ name: '', description: '', price: '' });
                  setShowProductForm(true);
                }}
                className="btn-primary"
              >
                Přidat produkt
              </button>
            </div>

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

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {products.map((product) => {
                const price = typeof product.price === 'string'
                  ? parseFloat(product.price)
                  : product.price;

                return (
                  <div key={product.id} className="card">
                    <h3 className="text-xl font-semibold mb-2">
                      {product.name}
                    </h3>
                    <p className="text-gray-600 mb-2 line-clamp-2">
                      {product.description}
                    </p>
                    <p className="text-lg font-bold text-primary-600 mb-4">
                      {price?.toFixed(2) || '0.00'} Kč
                    </p>
                    <div className="flex space-x-2">
                      <button
                        onClick={() => handleEditProduct(product)}
                        className="btn-secondary flex-1"
                      >
                        Upravit
                      </button>
                      <button
                        onClick={() => handleDeleteProduct(product.id)}
                        className="btn-danger flex-1"
                      >
                        Smazat
                      </button>
                    </div>
                  </div>
                );
              })}
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

        {/* Orders Tab */}
        {activeTab === 'orders' && (
          <div>
            <h2 className="text-2xl font-semibold mb-4">Všechny objednávky</h2>
            {orders.length > 0 ? (
              <div className="overflow-x-auto">
                <table className="w-full card">
                  <thead>
                    <tr className="border-b">
                      <th className="text-left py-2">ID</th>
                      <th className="text-left py-2">Produkt</th>
                      <th className="text-left py-2">Množství</th>
                      <th className="text-left py-2">Cena</th>
                      <th className="text-left py-2">Datum</th>
                    </tr>
                  </thead>
                  <tbody>
                    {orders.map((order) => {
                      const price = typeof order.Price === 'string'
                        ? parseFloat(order.Price)
                        : order.Price || order.totalPrice || 0;
                      const totalPrice = price * (order.quantity || 1);

                      return (
                        <tr key={order.id} className="border-b">
                          <td className="py-2">#{order.id}</td>
                          <td className="py-2">{order.productName}</td>
                          <td className="py-2">{order.quantity}</td>
                          <td className="py-2">{totalPrice.toFixed(2)} Kč</td>
                          <td className="py-2">
                            {formatDate(order.createdAt)}
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            ) : (
              <p className="text-gray-500 text-center py-8">
                Žádné objednávky
              </p>
            )}
          </div>
        )}
      </div>
    </ProtectedRoute>
  );
};

export default AdminPage;

