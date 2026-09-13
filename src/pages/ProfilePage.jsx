import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/useAuth';
import { getUserOrders, withdrawOrder } from '../services/orderService';
import { deleteAccount, updateUser, addUserAddress, getUserAddresses } from '../services/userService';
import { sendWebhook } from '../services/webhookService';
import ProtectedRoute from '../components/ProtectedRoute';
import LoadingSpinner from '../components/LoadingSpinner';
import { 
  Eye, 
  RotateCcw, 
  XCircle, 
  CheckCircle, 
  AlertCircle, 
  FileText, 
  Package, 
  Truck, 
  CreditCard,
  Building2,
  Info
} from 'lucide-react';

const ProfilePage = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [ordersError, setOrdersError] = useState('');
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deleteLoading, setDeleteLoading] = useState(false);
  const [deleteError, setDeleteError] = useState('');

  // --- Order detail & withdrawal modal states ---
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [withdrawalOrder, setWithdrawalOrder] = useState(null);
  const [withdrawalFormData, setWithdrawalFormData] = useState({
    bankAccount: '',
    reason: '',
    note: '',
    confirmed: false
  });
  const [withdrawalLoading, setWithdrawalLoading] = useState(false);
  const [withdrawalSuccess, setWithdrawalSuccess] = useState(false);
  const [withdrawalError, setWithdrawalError] = useState('');
  const [withdrawnOrderIds, setWithdrawnOrderIds] = useState([]);

  // --- Address / Profile form states ---
  const [profileData, setProfileData] = useState({
    firstName: user?.firstName || '',
    lastName: user?.lastName || '',
    street: '',
    city: '',
    phone: '',
    postalCode: '',
    country: ''
  });
  const [isEditingProfile, setIsEditingProfile] = useState(false);
  const [profileLoading, setProfileLoading] = useState(false);
  const [profileMessage, setProfileMessage] = useState({ type: '', text: '' });

  // Update form data when user context is loaded
  useEffect(() => {
    const loadProfileData = async () => {
      if (user) {
        try {
          const addresses = await getUserAddresses();
          if (addresses && addresses.length > 0) {
            // Bere poslední přidanou adresu ze seznamu
            const address = addresses[addresses.length - 1];
            setProfileData({
              firstName: address.recipientFirstName || user.firstName || '',
              lastName: address.recipientLastName || user.lastName || '',
              street: address.street || '',
              city: address.city || '',
              phone: address.phoneNumber || '',
              postalCode: address.postalCode || '',
              country: address.country || ''
            });
            return;
          }
        } catch (err) {
          console.error("Nepodařilo se načíst adresy:", err);
        }
        
        // Fallback pokud adresa neexistuje
        setProfileData({
          firstName: user.firstName || '',
          lastName: user.lastName || '',
          street: '',
          city: '',
          phone: '',
          postalCode: '',
          country: ''
        });
      }
    };
    loadProfileData();
  }, [user]);

  const handleProfileChange = (e) => {
    setProfileData({
      ...profileData,
      [e.target.name]: e.target.value
    });
  };

  const handleProfileSubmit = async (e) => {
    e.preventDefault();
    setProfileMessage({ type: '', text: '' });
    setProfileLoading(true);

    try {
      if (!user?.id) {
        throw new Error("Chybí ID uživatele. Zkuste se přihlásit znovu.");
      }
      
      // Update user info First Name, Last Name, and existing email
      await updateUser(user.id, {
        firstName: profileData.firstName,
        lastName: profileData.lastName,
        email: user.email // keep original email since email window was removed
      });

      // Insert address if at least one field is filled
      if (profileData.street || profileData.city || profileData.postalCode || profileData.country || profileData.phone) {
          await addUserAddress({
              recipientFirstName: profileData.firstName,
              recipientLastName: profileData.lastName,
              street: profileData.street,
              city: profileData.city,
              phoneNumber: profileData.phone,
              postalCode: profileData.postalCode,
              country: profileData.country
          });
      }

      setProfileMessage({ type: 'success', text: 'Profil a adresa byly úspěšně uloženy.' });
      setIsEditingProfile(false);
    } catch (error) {
      console.error('Chyba při ukládání profilu:', error);
      setProfileMessage({ 
        type: 'error', 
        text: error.response?.data?.message || error.response?.data?.error || error.message || 'Nepodařilo se uložit data.' 
      });
    } finally {
      setProfileLoading(false);
    }
  };

  useEffect(() => {
    const fetchOrders = async () => {
      try {
        setOrdersError('');
        const data = await getUserOrders();
        setOrders(data || []);
      } catch (error) {
        console.error('Chyba při načítání objednávek:', error);
        if (!error.response) {
          setOrdersError('Nepodařilo se připojit ke službě pro načtení objednávek.');
        } else {
          setOrdersError('Nepodařilo se načíst historii objednávek. Zkuste to prosím později.');
        }
      } finally {
        setLoading(false);
      }
    };

    fetchOrders();
  }, []);

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

  const handleDeleteAccount = async () => {
    setDeleteLoading(true);
    setDeleteError('');
    try {
      await deleteAccount();
      await sendWebhook('account_deleted', { username: user?.username });
      await logout();
      navigate('/');
    } catch (error) {
      console.error('Chyba při mazání účtu:', error);
      if (!error.response) {
        setDeleteError('Nepodařilo se připojit k serveru. Zkontrolujte připojení k internetu.');
      } else {
        setDeleteError(error.response.data?.message || error.response.data?.error || 'Nepodařilo se smazat účet. Zkuste to prosím znovu.');
      }
      setDeleteLoading(false);
    }
  };

  // --- Handlers for Order Detail & Withdrawal ---
  const handleOpenDetail = (order) => {
    setSelectedOrder(order);
  };

  const handleOpenWithdrawal = (order) => {
    setWithdrawalOrder(order);
    setWithdrawalFormData({
      bankAccount: '',
      reason: '',
      note: '',
      confirmed: false
    });
    setWithdrawalError('');
    setWithdrawalSuccess(false);
  };

  const handleWithdrawalChange = (e) => {
    const { name, value, type, checked } = e.target;
    setWithdrawalFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }));
  };

  const handleWithdrawalSubmit = async (e) => {
    e.preventDefault();
    if (!withdrawalFormData.bankAccount.trim()) {
      setWithdrawalError('Prosím vyplňte číslo bankovního účtu pro vrácení platby.');
      return;
    }
    if (!withdrawalFormData.confirmed) {
      setWithdrawalError('Je nutné potvrdit souhlas s odesláním odstoupení od smlouvy.');
      return;
    }

    setWithdrawalLoading(true);
    setWithdrawalError('');

    try {
      const payload = {
        orderId: withdrawalOrder.id,
        bankAccount: withdrawalFormData.bankAccount.trim(),
        reason: withdrawalFormData.reason,
        note: withdrawalFormData.note.trim(),
        username: user?.username,
        userEmail: user?.email,
        totalPrice: withdrawalOrder.totalPrice || withdrawalOrder.Price || 0,
        withdrawalDate: new Date().toISOString()
      };

      await withdrawOrder(withdrawalOrder.id, payload);
      await sendWebhook('order_withdrawal_requested', payload);

      setWithdrawnOrderIds(prev => [...prev, withdrawalOrder.id]);
      setWithdrawalSuccess(true);
    } catch (err) {
      console.error('Chyba při odesílání odstoupení od smlouvy:', err);
      setWithdrawalError(
        err.response?.data?.message || 
        err.response?.data?.error || 
        'Nepodařilo se odeslat odstoupení od smlouvy. Zkuste to prosím znovu nebo kontaktujte podporu.'
      );
    } finally {
      setWithdrawalLoading(false);
    }
  };

  const renderStatusBadge = (status, orderId) => {
    const isWithdrawn = withdrawnOrderIds.includes(orderId);
    if (isWithdrawn) {
      return (
        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-amber-100 text-amber-800 border border-amber-300">
          Odstoupeno od smlouvy
        </span>
      );
    }

    switch (status) {
      case 'PAID':
        return <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-blue-100 text-blue-800 border border-blue-200">Zaplaceno</span>;
      case 'SHIPPED':
        return <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-indigo-100 text-indigo-800 border border-indigo-200">Odesláno</span>;
      case 'DELIVERED':
        return <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-green-100 text-green-800 border border-green-200">Doručeno</span>;
      case 'RETURNED':
        return <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-amber-100 text-amber-800 border border-amber-200">Vráceno</span>;
      case 'CANCELLED':
        return <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-red-100 text-red-800 border border-red-200">Zrušeno</span>;
      case 'PENDING_PAYMENT':
        return <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-yellow-100 text-yellow-800 border border-yellow-200">Čeká na platbu</span>;
      default:
        return <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-gray-100 text-gray-800 border border-gray-200">{status || 'Vytvořeno'}</span>;
    }
  };

  return (
    <ProtectedRoute>
      <div className="container mx-auto px-4 py-8 max-w-6xl">
        <h1 className="text-3xl font-bold mb-6 text-gray-900">Můj zákaznický účet</h1>

        {/* Informace o uživateli */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 mb-8">
          <h2 className="text-2xl font-semibold mb-4 text-gray-900">Informace o účtu</h2>
          <div className="space-y-2">
            <p>
              <span className="font-semibold text-gray-700">Uživatelské jméno:</span>{' '}
              <span className="text-gray-900">{user?.username}</span>
            </p>
            <p>
              <span className="font-semibold text-gray-700">Role:</span>{' '}
              <span className="text-gray-900">
                {user?.roles?.map((r) => {
                  const role = typeof r === 'string' ? r : r.authority;
                  return role === 'ROLE_ADMIN' ? 'Administrátor' : 'Uživatel';
                }).join(', ') || 'Uživatel'}
              </span>
            </p>
          </div>

          {/* Upravitelný profil a adresa */}
          <div className="mt-8 pt-6 border-t border-gray-200">
            <div className="flex justify-between items-center mb-4">
              <h3 className="text-lg font-semibold text-gray-800">
                Osobní údaje a doručovací adresa
              </h3>
              {!isEditingProfile && (
                <button
                  onClick={() => setIsEditingProfile(true)}
                  className="bg-gray-100 hover:bg-gray-200 text-gray-700 px-4 py-2 rounded-lg border border-gray-300 text-sm font-medium transition-colors"
                >
                  Upravit údaje
                </button>
              )}
            </div>

            {profileMessage.text && (
              <div className={`p-3 rounded-lg mb-4 text-sm font-medium ${
                profileMessage.type === 'error' 
                  ? 'bg-red-50 text-red-700 border border-red-200' 
                  : 'bg-green-50 text-green-700 border border-green-200'
              }`}>
                {profileMessage.text}
              </div>
            )}

            {isEditingProfile ? (
              <form onSubmit={handleProfileSubmit} className="space-y-4">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Jméno</label>
                    <input 
                      type="text" 
                      name="firstName" 
                      value={profileData.firstName} 
                      onChange={handleProfileChange} 
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 text-sm" 
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Příjmení</label>
                    <input 
                      type="text" 
                      name="lastName" 
                      value={profileData.lastName} 
                      onChange={handleProfileChange} 
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 text-sm" 
                    />
                  </div>
                  <div className="md:col-span-2">
                    <label className="block text-sm font-medium text-gray-700 mb-1">Ulice a číslo popisné</label>
                    <input 
                      type="text" 
                      name="street" 
                      value={profileData.street} 
                      onChange={handleProfileChange} 
                      placeholder="Např. Václavské náměstí 1" 
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 text-sm" 
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Město</label>
                    <input 
                      type="text" 
                      name="city" 
                      value={profileData.city} 
                      onChange={handleProfileChange} 
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 text-sm" 
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Telefon</label>
                    <input 
                      type="text" 
                      name="phone" 
                      value={profileData.phone} 
                      onChange={handleProfileChange} 
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 text-sm" 
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">PSČ</label>
                    <input 
                      type="text" 
                      name="postalCode" 
                      value={profileData.postalCode} 
                      onChange={handleProfileChange} 
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 text-sm" 
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Země</label>
                    <input 
                      type="text" 
                      name="country" 
                      value={profileData.country} 
                      onChange={handleProfileChange} 
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 text-sm" 
                    />
                  </div>
                </div>
                <div className="flex gap-3 justify-end pt-2">
                  <button 
                    type="button" 
                    onClick={() => setIsEditingProfile(false)} 
                    disabled={profileLoading} 
                    className="px-4 py-2 rounded-lg border border-gray-300 bg-white text-gray-700 hover:bg-gray-50 text-sm font-medium"
                  >
                    Zrušit
                  </button>
                  <button 
                    type="submit" 
                    disabled={profileLoading} 
                    className="px-4 py-2 rounded-lg bg-primary-600 hover:bg-primary-700 text-white font-medium text-sm transition-colors"
                  >
                    {profileLoading ? 'Ukládám...' : 'Uložit údaje'}
                  </button>
                </div>
              </form>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm text-gray-700">
                <div>
                  <span className="block font-semibold text-gray-900 mb-0.5">Jméno a příjmení</span>
                  {profileData.firstName || profileData.lastName ? `${profileData.firstName} ${profileData.lastName}` : <span className="text-gray-400 italic">Nenastaveno</span>}
                </div>
                <div>
                  <span className="block font-semibold text-gray-900 mb-0.5">Email</span>
                  {user?.email || <span className="text-gray-400 italic">Nenastaveno</span>}
                </div>
                <div className="md:col-span-2">
                  <span className="block font-semibold text-gray-900 mb-0.5">Adresa dodání (Naposledy vyplněná)</span>
                  {profileData.street || profileData.city || profileData.postalCode || profileData.country || profileData.phone ? (
                    <div className="space-y-0.5 text-gray-800">
                      {profileData.street && <div>{profileData.street}</div>}
                      {(profileData.city || profileData.postalCode) && <div>{profileData.city}{profileData.postalCode && `, ${profileData.postalCode}`}</div>}
                      {profileData.country && <div>{profileData.country}</div>}
                    </div>
                  ) : <span className="text-gray-400 italic">Nenastaveno</span>}
                </div>
                <div>
                  <span className="block font-semibold text-gray-900 mb-0.5">Telefon</span>
                  {profileData.phone || <span className="text-gray-400 italic">Nenastaveno</span>}
                </div>
              </div>
            )}
          </div>

          {/* Tlačítko smazat účet */}
          <div className="mt-8 pt-6 border-t border-gray-200">
            <h3 className="text-base font-semibold text-gray-900 mb-1">
              Nebezpečná zóna
            </h3>
            <p className="text-sm text-gray-500 mb-3">
              Smazání účtu je nevratná akce. Všechna vaše data budou trvale odstraněna.
            </p>
            <button
              id="btn-delete-account"
              onClick={() => setShowDeleteModal(true)}
              className="bg-red-600 hover:bg-red-700 text-white px-4 py-2 rounded-lg font-semibold text-sm transition-colors"
            >
              Smazat účet
            </button>
          </div>
        </div>

        {/* Historie objednávek */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 mb-6">
            <div>
              <h2 className="text-2xl font-semibold text-gray-900">Historie objednávek</h2>
              <p className="text-sm text-gray-500 mt-1">
                Přehled všech vašich objednávek, jejich stav a možnost odstoupení od kupní smlouvy ve 14denní lhůtě.
              </p>
            </div>
            {orders.length > 0 && (
              <span className="text-xs font-semibold px-3 py-1 bg-gray-100 text-gray-600 rounded-full w-fit">
                Celkem objednávek: {orders.length}
              </span>
            )}
          </div>

          {loading ? (
            <div className="flex justify-center py-12">
              <LoadingSpinner />
            </div>
          ) : ordersError ? (
            <div className="p-4 bg-red-50 text-red-700 rounded-lg border border-red-200 flex items-center gap-2">
              <AlertCircle size={20} className="flex-shrink-0" />
              <span>{ordersError}</span>
            </div>
          ) : orders.length > 0 ? (
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="border-b border-gray-200 text-xs font-semibold text-gray-500 uppercase tracking-wider bg-gray-50">
                    <th className="py-3 px-4">Číslo obj.</th>
                    <th className="py-3 px-4">Položky</th>
                    <th className="py-3 px-4">Cena celkem</th>
                    <th className="py-3 px-4">Stav</th>
                    <th className="py-3 px-4">Datum</th>
                    <th className="py-3 px-4 text-right">Akce</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100 text-sm">
                  {orders.map((order) => {
                    const productNames = order.items?.map(i => i.productName).join(', ') || order.productName || 'Zboží v objednávce';
                    const totalQuantity = order.items?.reduce((sum, i) => sum + i.quantity, 0) || order.quantity || 1;
                    const rawPrice = order.totalPrice || order.Price || 0;
                    const priceNum = typeof rawPrice === 'string' ? Number.parseFloat(rawPrice) : rawPrice;
                    const isWithdrawn = withdrawnOrderIds.includes(order.id);

                    return (
                      <tr key={order.id} className="hover:bg-gray-50/80 transition-colors">
                        <td className="py-4 px-4 font-bold text-gray-900 whitespace-nowrap">
                          #{order.id}
                        </td>
                        <td className="py-4 px-4">
                          <div className="font-medium text-gray-900 max-w-xs truncate" title={productNames}>
                            {productNames}
                          </div>
                          <div className="text-xs text-gray-500 mt-0.5">
                            {totalQuantity} {totalQuantity === 1 ? 'položka' : (totalQuantity < 5 ? 'položky' : 'položek')}
                          </div>
                        </td>
                        <td className="py-4 px-4 font-semibold text-gray-900 whitespace-nowrap">
                          {priceNum.toFixed(2)} Kč
                        </td>
                        <td className="py-4 px-4 whitespace-nowrap">
                          {renderStatusBadge(order.status, order.id)}
                        </td>
                        <td className="py-4 px-4 text-gray-600 whitespace-nowrap text-xs">
                          {formatDate(order.createdAt)}
                        </td>
                        <td className="py-4 px-4 text-right whitespace-nowrap">
                          <div className="flex items-center justify-end gap-2">
                            {/* Tlačítko Detail objednávky */}
                            <button
                              id={`btn-detail-order-${order.id}`}
                              onClick={() => handleOpenDetail(order)}
                              className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium text-gray-700 bg-white hover:bg-gray-50 border border-gray-300 rounded-md transition-colors shadow-sm"
                              title="Zobrazit detail objednávky"
                            >
                              <Eye size={15} />
                              <span>Detail</span>
                            </button>

                            {/* Tlačítko Odstoupit od smlouvy - trvale viditelné, textově jasně označené */}
                            <button
                              id={`btn-withdraw-order-${order.id}`}
                              data-testid={`btn-withdraw-order-${order.id}`}
                              onClick={() => handleOpenWithdrawal(order)}
                              disabled={isWithdrawn}
                              className={`inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold rounded-md transition-all shadow-sm ${
                                isWithdrawn
                                  ? 'bg-gray-100 text-gray-400 border border-gray-200 cursor-not-allowed'
                                  : 'bg-amber-50 hover:bg-amber-100 text-amber-900 border border-amber-300 hover:border-amber-400 active:scale-[0.98]'
                              }`}
                              title="Odstoupit od kupní smlouvy v zákonné 14denní lhůtě"
                            >
                              <RotateCcw size={14} className={isWithdrawn ? 'text-gray-400' : 'text-amber-700'} />
                              <span>Odstoupit od smlouvy</span>
                            </button>
                          </div>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          ) : (
            <div className="text-center py-12">
              <Package size={48} className="mx-auto text-gray-300 mb-3" />
              <p className="text-gray-500 font-medium">
                Zatím nemáte žádné objednávky
              </p>
              <button
                onClick={() => navigate('/products')}
                className="mt-4 inline-flex items-center px-4 py-2 text-sm font-medium text-white bg-primary-600 hover:bg-primary-700 rounded-lg transition-colors"
              >
                Prohlédnout nabídku produktů
              </button>
            </div>
          )}
        </div>
      </div>

      {/* MODAL: DETAIL OBJEDNÁVKY */}
      {selectedOrder && (
        <div className="fixed inset-0 z-[70] flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fade-in">
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-3xl max-h-[90vh] flex flex-col overflow-hidden border border-gray-100">
            {/* Modal Header */}
            <div className="sticky top-0 bg-white border-b border-gray-100 px-6 py-4 flex items-center justify-between z-10">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-xl bg-primary-50 flex items-center justify-center text-primary-600">
                  <Package size={22} />
                </div>
                <div>
                  <h2 className="text-xl font-bold text-gray-900">
                    Detail objednávky <span className="text-primary-600">#{selectedOrder.id}</span>
                  </h2>
                  <p className="text-xs text-gray-500">
                    Vytvořeno: {formatDate(selectedOrder.createdAt)}
                  </p>
                </div>
              </div>
              <button
                id="btn-close-detail-modal"
                onClick={() => setSelectedOrder(null)}
                className="p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-100 rounded-full transition-colors"
              >
                <XCircle size={24} />
              </button>
            </div>

            {/* Modal Body */}
            <div className="p-6 overflow-y-auto space-y-6">
              {/* Status and Summary Cards */}
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div className="bg-gray-50 p-4 rounded-xl border border-gray-100">
                  <span className="text-xs font-semibold text-gray-400 uppercase tracking-wider block mb-1">
                    Stav objednávky
                  </span>
                  <div>{renderStatusBadge(selectedOrder.status, selectedOrder.id)}</div>
                </div>

                <div className="bg-gray-50 p-4 rounded-xl border border-gray-100">
                  <span className="text-xs font-semibold text-gray-400 uppercase tracking-wider block mb-1">
                    Celková částka
                  </span>
                  <div className="text-lg font-bold text-gray-900">
                    {(Number(selectedOrder.totalPrice || selectedOrder.Price || 0)).toFixed(2)} Kč
                  </div>
                </div>

                <div className="bg-gray-50 p-4 rounded-xl border border-gray-100">
                  <span className="text-xs font-semibold text-gray-400 uppercase tracking-wider block mb-1">
                    Způsob platby / dopravy
                  </span>
                  <div className="text-xs text-gray-700 font-medium">
                    {selectedOrder.paymentMethod || 'Online platba'} • {selectedOrder.shippingMethod || 'Dopravce'}
                  </div>
                </div>
              </div>

              {/* Delivery Address & Customer Info */}
              <div className="bg-gray-50 p-4 rounded-xl border border-gray-100">
                <h3 className="text-xs font-semibold text-gray-400 uppercase tracking-wider mb-2 flex items-center gap-1.5">
                  <Truck size={16} className="text-gray-500" />
                  Doručovací údaje
                </h3>
                {selectedOrder.deliveryAddress || selectedOrder.address ? (
                  <div className="text-sm text-gray-700 space-y-0.5">
                    <p className="font-semibold text-gray-900">
                      {(selectedOrder.deliveryAddress?.recipientFirstName || selectedOrder.address?.recipientFirstName || '')}{' '}
                      {(selectedOrder.deliveryAddress?.recipientLastName || selectedOrder.address?.recipientLastName || '')}
                    </p>
                    <p>{selectedOrder.deliveryAddress?.street || selectedOrder.address?.street}</p>
                    <p>
                      {selectedOrder.deliveryAddress?.postalCode || selectedOrder.address?.postalCode}{' '}
                      {selectedOrder.deliveryAddress?.city || selectedOrder.address?.city}
                    </p>
                    <p>{selectedOrder.deliveryAddress?.country || selectedOrder.address?.country}</p>
                    {(selectedOrder.deliveryAddress?.phoneNumber || selectedOrder.address?.phoneNumber) && (
                      <p className="text-xs text-gray-500 pt-1">
                        Tel: {selectedOrder.deliveryAddress?.phoneNumber || selectedOrder.address?.phoneNumber}
                      </p>
                    )}
                  </div>
                ) : (
                  <p className="text-sm text-gray-500 italic">
                    Doručovací adresa: dle údajů v profilu ({profileData.firstName} {profileData.lastName}, {profileData.city || 'Praha'})
                  </p>
                )}
              </div>

              {/* Order Items Table */}
              <div>
                <h3 className="text-xs font-semibold text-gray-400 uppercase tracking-wider mb-3 flex items-center gap-1.5">
                  <Package size={16} className="text-gray-500" />
                  Položky objednávky
                </h3>
                <div className="border border-gray-200 rounded-xl overflow-hidden shadow-sm">
                  <table className="w-full text-sm text-left">
                    <thead className="bg-gray-50 text-gray-500 text-xs font-semibold uppercase tracking-wider border-b border-gray-200">
                      <tr>
                        <th className="px-4 py-3">Produkt</th>
                        <th className="px-4 py-3 text-right">Cena / ks</th>
                        <th className="px-4 py-3 text-center">Množství</th>
                        <th className="px-4 py-3 text-right">Celkem</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-100">
                      {selectedOrder.items && selectedOrder.items.length > 0 ? (
                        selectedOrder.items.map((item, idx) => (
                          <tr key={idx} className="hover:bg-gray-50/60">
                            <td className="px-4 py-3 font-medium text-gray-900">
                              {item.productName || 'Zboží'}
                            </td>
                            <td className="px-4 py-3 text-right text-gray-600">
                              {Number(item.price || 0).toFixed(2)} Kč
                            </td>
                            <td className="px-4 py-3 text-center font-semibold text-gray-800">
                              {item.quantity} ks
                            </td>
                            <td className="px-4 py-3 text-right font-bold text-gray-900">
                              {Number(item.totalPrice || (item.price * item.quantity) || 0).toFixed(2)} Kč
                            </td>
                          </tr>
                        ))
                      ) : (
                        <tr>
                          <td className="px-4 py-3 font-medium text-gray-900">
                            {selectedOrder.productName || 'Položky objednávky'}
                          </td>
                          <td className="px-4 py-3 text-right text-gray-600">
                            {(Number(selectedOrder.totalPrice || selectedOrder.Price || 0)).toFixed(2)} Kč
                          </td>
                          <td className="px-4 py-3 text-center font-semibold text-gray-800">
                            {selectedOrder.quantity || 1} ks
                          </td>
                          <td className="px-4 py-3 text-right font-bold text-gray-900">
                            {(Number(selectedOrder.totalPrice || selectedOrder.Price || 0)).toFixed(2)} Kč
                          </td>
                        </tr>
                      )}
                    </tbody>
                    <tfoot className="bg-gray-50/80 font-bold border-t border-gray-200">
                      <tr>
                        <td colSpan="3" className="px-4 py-3 text-gray-700 text-right">
                          Celková částka objednávky:
                        </td>
                        <td className="px-4 py-3 text-right text-primary-600 text-base">
                          {(Number(selectedOrder.totalPrice || selectedOrder.Price || 0)).toFixed(2)} Kč
                        </td>
                      </tr>
                    </tfoot>
                  </table>
                </div>
              </div>
            </div>

            {/* Modal Footer with TRVALE VIDITELNÉ A TEXTOVĚ JASNĚ OZNAČENÉ TLAČÍTKO "ODSTOUPIT OD SMLOUVY" */}
            <div className="bg-gray-50 border-t border-gray-200 px-6 py-4 flex flex-col sm:flex-row items-center justify-between gap-3">
              <div className="text-xs text-gray-500 flex items-center gap-1.5">
                <Info size={16} className="text-gray-400 flex-shrink-0" />
                <span>Zákonné právo na odstoupení od smlouvy do 14 dnů bez udání důvodu.</span>
              </div>

              <div className="flex items-center gap-3 w-full sm:w-auto justify-end">
                <button
                  onClick={() => setSelectedOrder(null)}
                  className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 hover:bg-gray-100 rounded-lg transition-colors"
                >
                  Zavřít
                </button>

                {/* Tlačítko Odstoupit od smlouvy v detailu objednávky */}
                <button
                  id={`btn-modal-withdraw-order-${selectedOrder.id}`}
                  data-testid={`btn-modal-withdraw-order-${selectedOrder.id}`}
                  onClick={() => {
                    const current = selectedOrder;
                    setSelectedOrder(null);
                    handleOpenWithdrawal(current);
                  }}
                  disabled={withdrawnOrderIds.includes(selectedOrder.id)}
                  className={`inline-flex items-center gap-2 px-4 py-2 text-sm font-semibold rounded-lg shadow-sm transition-all ${
                    withdrawnOrderIds.includes(selectedOrder.id)
                      ? 'bg-gray-200 text-gray-500 cursor-not-allowed border border-gray-300'
                      : 'bg-amber-600 hover:bg-amber-700 active:bg-amber-800 text-white border border-amber-600'
                  }`}
                >
                  <RotateCcw size={16} />
                  <span>Odstoupit od smlouvy</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* MODAL: ODSTOUPENÍ OD KUPNÍ SMLOUVY (FORMULÁŘ) */}
      {withdrawalOrder && (
        <div className="fixed inset-0 z-[70] flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fade-in">
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-2xl max-h-[90vh] flex flex-col overflow-hidden border border-gray-100">
            {/* Modal Header */}
            <div className="sticky top-0 bg-white border-b border-gray-100 px-6 py-4 flex items-center justify-between z-10">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-xl bg-amber-50 flex items-center justify-center text-amber-700">
                  <FileText size={22} />
                </div>
                <div>
                  <h2 className="text-xl font-bold text-gray-900">
                    Odstoupení od kupní smlouvy
                  </h2>
                  <p className="text-xs text-gray-500">
                    Objednávka <strong className="text-gray-800">#{withdrawalOrder.id}</strong> • vytvořena {formatDate(withdrawalOrder.createdAt)}
                  </p>
                </div>
              </div>
              <button
                id="btn-close-withdrawal-modal"
                onClick={() => setWithdrawalOrder(null)}
                className="p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-100 rounded-full transition-colors"
              >
                <XCircle size={24} />
              </button>
            </div>

            {/* Modal Content */}
            <div className="p-6 overflow-y-auto space-y-6">
              {withdrawalSuccess ? (
                <div className="text-center py-6 space-y-4">
                  <div className="w-16 h-16 bg-green-100 text-green-600 rounded-full flex items-center justify-center mx-auto">
                    <CheckCircle size={36} />
                  </div>
                  <h3 className="text-xl font-bold text-gray-900">
                    Odstoupení od smlouvy bylo úspěšně zaevidováno
                  </h3>
                  <p className="text-sm text-gray-600 max-w-md mx-auto leading-relaxed">
                    Vaše oznámení o odstoupení od smlouvy k objednávce <strong>#{withdrawalOrder.id}</strong> bylo úspěšně přijato.
                    Peníze ve výši <strong>{(Number(withdrawalOrder.totalPrice || withdrawalOrder.Price || 0)).toFixed(2)} Kč</strong> vám budou vráceny na bankovní účet <strong>{withdrawalFormData.bankAccount}</strong> do 14 dnů od doručení vráceného zboží.
                  </p>

                  <div className="bg-amber-50 border border-amber-200 rounded-xl p-4 text-left text-xs text-amber-900 space-y-2 mt-4">
                    <div className="font-bold text-sm flex items-center gap-1.5">
                      <Truck size={16} />
                      Instrukce pro vrácení zboží:
                    </div>
                    <p>Zboží prosím bezpečně zabalte a odešlete na adresu:</p>
                    <div className="bg-white/80 p-2.5 rounded-lg border border-amber-300 font-medium">
                      E-stop s.r.o. – Oddělení vratek<br />
                      Průmyslová 1234/5<br />
                      100 00 Praha 10<br />
                      Česká republika
                    </div>
                    <p className="text-amber-800">
                      Do balíčku prosím přiložte lístek s číslem objednávky <strong>#{withdrawalOrder.id}</strong> pro rychlou identifikaci.
                    </p>
                  </div>

                  <div className="pt-4">
                    <button
                      id="btn-withdrawal-success-done"
                      onClick={() => setWithdrawalOrder(null)}
                      className="px-6 py-2.5 bg-primary-600 hover:bg-primary-700 text-white font-semibold rounded-lg text-sm transition-colors"
                    >
                      Rozumím a zavřít
                    </button>
                  </div>
                </div>
              ) : (
                <form onSubmit={handleWithdrawalSubmit} className="space-y-5">
                  {/* Zákonné poučení */}
                  <div className="bg-blue-50 border border-blue-200 rounded-xl p-4 text-xs text-blue-900 leading-relaxed flex items-start gap-3">
                    <Info size={18} className="text-blue-600 flex-shrink-0 mt-0.5" />
                    <div>
                      <strong className="block text-blue-950 font-semibold mb-0.5">
                        Zákonná lhůta pro odstoupení od smlouvy (§ 1829 NOZ)
                      </strong>
                      Jako spotřebitel máte právo odstoupit od smlouvy bez udání důvodu ve lhůtě 14 dnů ode dne převzetí zboží. Vyplněním tohoto formuláře uplatňujete své právo na odstoupení od kupní smlouvy.
                    </div>
                  </div>

                  {/* Summary of items */}
                  <div className="bg-gray-50 rounded-xl p-4 border border-gray-100">
                    <div className="flex justify-between items-center mb-2">
                      <span className="text-xs font-semibold text-gray-500 uppercase">Objednané položky:</span>
                      <span className="text-xs font-bold text-gray-900">
                        Celkem k vrácení: {(Number(withdrawalOrder.totalPrice || withdrawalOrder.Price || 0)).toFixed(2)} Kč
                      </span>
                    </div>
                    <ul className="text-xs text-gray-700 space-y-1 list-disc list-inside">
                      {withdrawalOrder.items && withdrawalOrder.items.length > 0 ? (
                        withdrawalOrder.items.map((item, idx) => (
                          <li key={idx} className="truncate">
                            <span className="font-medium">{item.productName || 'Zboží'}</span> ({item.quantity} ks) – {Number(item.totalPrice || (item.price * item.quantity) || 0).toFixed(2)} Kč
                          </li>
                        ))
                      ) : (
                        <li>{withdrawalOrder.productName || 'Položky objednávky'} (1 ks)</li>
                      )}
                    </ul>
                  </div>

                  {withdrawalError && (
                    <div className="p-3 bg-red-50 text-red-700 text-xs font-medium rounded-lg border border-red-200 flex items-center gap-2">
                      <AlertCircle size={16} className="flex-shrink-0" />
                      <span>{withdrawalError}</span>
                    </div>
                  )}

                  {/* Pole: Číslo bankovního účtu */}
                  <div>
                    <label className="block text-sm font-semibold text-gray-800 mb-1">
                      Číslo bankovního účtu pro vrácení peněz <span className="text-red-500">*</span>
                    </label>
                    <input
                      id="input-withdrawal-bank-account"
                      type="text"
                      name="bankAccount"
                      required
                      placeholder="např. 1234567890/0100 nebo CZ..."
                      value={withdrawalFormData.bankAccount}
                      onChange={handleWithdrawalChange}
                      className="w-full px-3.5 py-2.5 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-amber-500 focus:border-amber-500 shadow-sm"
                    />
                    <p className="text-xs text-gray-500 mt-1">
                      Na tento účet vám poukážeme peněžní prostředky včetně poštovného po doručení zboží.
                    </p>
                  </div>

                  {/* Pole: Důvod odstoupení (nepovinné) */}
                  <div>
                    <label className="block text-sm font-semibold text-gray-800 mb-1">
                      Důvod odstoupení od smlouvy <span className="text-gray-400 font-normal text-xs">(nepovinné)</span>
                    </label>
                    <select
                      id="select-withdrawal-reason"
                      name="reason"
                      value={withdrawalFormData.reason}
                      onChange={handleWithdrawalChange}
                      className="w-full px-3.5 py-2.5 border border-gray-300 rounded-lg text-sm bg-white focus:ring-2 focus:ring-amber-500 focus:border-amber-500 shadow-sm"
                    >
                      <option value="">-- Vyberte důvod (nepovinné) --</option>
                      <option value="NOT_AS_EXPECTED">Zboží neodpovídá popisu nebo představám</option>
                      <option value="WRONG_SIZE">Nevhodná velikost nebo parametry</option>
                      <option value="ORDERED_BY_MISTAKE">Objednáno omylem</option>
                      <option value="DAMAGED_DEFECTIVE">Zboží dorazilo poškozené nebo nefunkční</option>
                      <option value="LATE_DELIVERY">Zpožděné doručení</option>
                      <option value="OTHER">Jiný důvod</option>
                    </select>
                  </div>

                  {/* Pole: Poznámka zákazníka */}
                  <div>
                    <label className="block text-sm font-semibold text-gray-800 mb-1">
                      Doplňující poznámka <span className="text-gray-400 font-normal text-xs">(nepovinné)</span>
                    </label>
                    <textarea
                      id="textarea-withdrawal-note"
                      name="note"
                      rows={2}
                      placeholder="Jakékoliv doplňující informace pro oddělení vratek..."
                      value={withdrawalFormData.note}
                      onChange={handleWithdrawalChange}
                      className="w-full px-3.5 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-amber-500 focus:border-amber-500 shadow-sm"
                    />
                  </div>

                  {/* Informace o adrese skladu pro vrácení */}
                  <div className="bg-gray-50 border border-gray-200 rounded-xl p-3.5 text-xs text-gray-700 space-y-1">
                    <div className="font-semibold text-gray-900 flex items-center gap-1.5">
                      <Building2 size={15} className="text-gray-600" />
                      Kam zboží zaslat:
                    </div>
                    <p className="text-gray-600">
                      <strong>E-stop s.r.o. – Oddělení vratek</strong>, Průmyslová 1234/5, 100 00 Praha 10.
                    </p>
                    <p className="text-gray-500 text-[11px]">
                      Zboží zašlete bez zbytečného odkladu, nejpozději do 14 dnů od odeslání tohoto oznámení.
                    </p>
                  </div>

                  {/* Potvrzovací checkbox */}
                  <div className="flex items-start gap-2.5 pt-1">
                    <input
                      id="checkbox-withdrawal-confirm"
                      type="checkbox"
                      name="confirmed"
                      checked={withdrawalFormData.confirmed}
                      onChange={handleWithdrawalChange}
                      className="mt-1 h-4 w-4 rounded border-gray-300 text-amber-600 focus:ring-amber-500 cursor-pointer"
                    />
                    <label htmlFor="checkbox-withdrawal-confirm" className="text-xs text-gray-700 cursor-pointer leading-tight">
                      <strong>Tímto prohlašuji, že odstupuji od kupní smlouvy</strong> k uvedené objednávce ve 14denní lhůtě a zavazuji se zaslat nepoužité zboží zpět prodejci.
                    </label>
                  </div>

                  {/* Tlačítka formuláře */}
                  <div className="flex items-center justify-end gap-3 pt-4 border-t border-gray-100">
                    <button
                      type="button"
                      id="btn-cancel-withdrawal"
                      onClick={() => setWithdrawalOrder(null)}
                      disabled={withdrawalLoading}
                      className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 hover:bg-gray-50 rounded-lg transition-colors"
                    >
                      Zrušit
                    </button>
                    <button
                      type="submit"
                      id="btn-submit-withdrawal"
                      disabled={withdrawalLoading || !withdrawalFormData.confirmed}
                      className="inline-flex items-center gap-2 px-5 py-2 text-sm font-semibold text-white bg-amber-600 hover:bg-amber-700 active:bg-amber-800 disabled:opacity-50 disabled:cursor-not-allowed rounded-lg shadow-sm transition-colors"
                    >
                      {withdrawalLoading ? 'Odesílám...' : 'Odeslat odstoupení od smlouvy'}
                    </button>
                  </div>
                </form>
              )}
            </div>
          </div>
        </div>
      )}

      {/* Potvrzovací modal pro smazání účtu */}
      {showDeleteModal && (
        <div
          style={{
            position: 'fixed',
            inset: 0,
            backgroundColor: 'rgba(0, 0, 0, 0.5)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 1000,
          }}
        >
          <div
            style={{
              backgroundColor: '#ffffff',
              borderRadius: '0.75rem',
              padding: '2rem',
              maxWidth: '28rem',
              width: '90%',
              boxShadow: '0 20px 60px rgba(0,0,0,0.3)',
            }}
          >
            <h2 style={{ fontSize: '1.25rem', fontWeight: '700', marginBottom: '0.75rem', color: '#111827' }}>
              ⚠️ Smazat účet?
            </h2>
            <p style={{ color: '#6b7280', marginBottom: '1.5rem', lineHeight: '1.6' }}>
              Opravdu chcete smazat svůj účet <strong>{user?.username}</strong>?
              Tato akce je <strong>nevratná</strong> a všechna vaše data budou trvale odstraněna.
            </p>

            {deleteError && (
              <p style={{ color: '#dc2626', marginBottom: '1rem', fontSize: '0.875rem' }}>
                {deleteError}
              </p>
            )}

            <div style={{ display: 'flex', gap: '0.75rem', justifyContent: 'flex-end' }}>
              <button
                id="btn-cancel-delete"
                onClick={() => { setShowDeleteModal(false); setDeleteError(''); }}
                disabled={deleteLoading}
                style={{
                  padding: '0.5rem 1.25rem',
                  borderRadius: '0.375rem',
                  border: '1px solid #d1d5db',
                  backgroundColor: '#ffffff',
                  color: '#374151',
                  cursor: 'pointer',
                  fontWeight: '500',
                }}
              >
                Zrušit
              </button>
              <button
                id="btn-confirm-delete"
                onClick={handleDeleteAccount}
                disabled={deleteLoading}
                style={{
                  padding: '0.5rem 1.25rem',
                  borderRadius: '0.375rem',
                  border: 'none',
                  backgroundColor: deleteLoading ? '#fca5a5' : '#dc2626',
                  color: '#ffffff',
                  cursor: deleteLoading ? 'not-allowed' : 'pointer',
                  fontWeight: '600',
                }}
              >
                {deleteLoading ? 'Mazání...' : 'Ano, smazat účet'}
              </button>
            </div>
          </div>
        </div>
      )}
    </ProtectedRoute>
  );
};

export default ProfilePage;
