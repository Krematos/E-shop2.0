import React, { useState, useEffect } from "react";
import { createProduct, updateProduct } from "../services/productService"; // Ujisti se o cestě
import { getImageUrl } from "../utils/urlUtils";

const AdminAddProductForm = ({ initialData, onProductSaved, onCancel }) => {
  const isEditing = !!initialData;

  // -- STATE --
  const [formData, setFormData] = useState({
    name: '',         // V DB se to jmenuje 'name'
    description: '',
    category: '',
    price: '',
    currency: 'Kč',   // Volitelné, pokud to DB podporuje
  });

  const [images, setImages] = useState([]);
  const [errors, setErrors] = useState({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  // -- INITIALIZATION (Předvyplnění při editaci) --
  useEffect(() => {
    if (initialData) {
      setFormData({
        name: initialData.name || '',
        description: initialData.description || '',
        category: initialData.category || '',
        price: initialData.price || '',
        currency: 'Kč',
      });

      if (initialData.images && Array.isArray(initialData.images)) {
        const existingImages = initialData.images.map((filename, index) => ({
          file: null,
          preview: getImageUrl(filename),
          id: `existing-${index}`,
          isExisting: true
        }));
        setImages(existingImages);
      }
    }
  }, [initialData]);

  // -- HANDLERS --

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value,
    }));
    // Smazání chyby při psaní
    if (errors[name]) setErrors(prev => ({ ...prev, [name]: null }));
  };

  const handleImageSelect = (e) => {
    const files = Array.from(e.target.files);
    processFiles(files);
  };

  // Zpracování souborů (Drag&Drop i Input)
  const processFiles = (files) => {
    const newImages = files.map(file => ({
      file: file,
      preview: URL.createObjectURL(file),
      id: Date.now() + Math.random(),
      isExisting: false
    }));
    setImages(prev => [...prev, ...newImages]);
  };

  const handleRemoveImage = (index) => {
    setImages(prev => prev.filter((_, i) => i !== index));
  };

  // Drag & Drop Handlery
  const handleDragOver = (e) => { e.preventDefault(); };
  const handleDrop = (e) => {
    e.preventDefault();
    const files = Array.from(e.dataTransfer.files).filter(f => f.type.startsWith('image/'));
    processFiles(files);
  };

  // Validace
  const validateForm = () => {
    const newErrors = {};
    if (!formData.name.trim()) newErrors.name = 'Název je povinný';
    if (!formData.description.trim()) newErrors.description = 'Popis je povinný';
    if (!formData.price || formData.price <= 0) newErrors.price = 'Cena musí být kladná';

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  // Odeslání formuláře
  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validateForm()) return;

    setIsSubmitting(true);

    try {
      // Příprava dat pro service layer
      const productPayload = {
        ...formData,
        images: images, // Service layer si to přebere (oddělí File objekty)
      };

      if (isEditing) {
        // UPDATE
        await updateProduct(initialData.id, productPayload);
      } else {
        // CREATE
        await createProduct(productPayload);
      }

      // Úspěch
      if (onProductSaved) onProductSaved();

    } catch (error) {
      console.error("Chyba při ukládání:", error);
      setErrors({ submit: 'Nepodařilo se uložit produkt. Zkuste to znovu.' });
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="bg-white rounded-lg shadow-md p-6 md:p-8">
      <h2 className="text-2xl font-bold mb-6 text-gray-800">
        {isEditing ? `Upravit produkt: ${initialData.name}` : 'Vložit nový inzerát'}
      </h2>

      {errors.submit && (
        <div className="mb-4 p-3 bg-red-100 border border-red-200 text-red-700 rounded">
          {errors.submit}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-6">

        {/* Název */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Název produktu</label>
          <input
            type="text"
            name="name"
            value={formData.name}
            onChange={handleChange}
            className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 ${errors.name ? 'border-red-500' : 'border-gray-300'}`}
            placeholder="Např. iPhone 13 Pro"
          />
          {errors.name && <p className="text-red-500 text-sm mt-1">{errors.name}</p>}
        </div>

        {/* Popis */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Popis</label>
          <textarea
            name="description"
            value={formData.description}
            onChange={handleChange}
            rows={4}
            className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 ${errors.description ? 'border-red-500' : 'border-gray-300'}`}
          />
          {errors.description && <p className="text-red-500 text-sm mt-1">{errors.description}</p>}
        </div>

        {/* Kategorie a Cena */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Kategorie</label>
            <select
              name="category"
              value={formData.category}
              onChange={handleChange}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
            >
              <option value="">Vyberte kategorii</option>
              <option value="Elektronika">Elektronika</option>
              <option value="Oblečení">Oblečení</option>
              <option value="Domácnost">Domácnost</option>
              <option value="Ostatní">Ostatní</option>
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Cena</label>
            <input
              type="number"
              name="price"
              value={formData.price}
              onChange={handleChange}
              min="0"
              className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 ${errors.price ? 'border-red-500' : 'border-gray-300'}`}
            />
            {errors.price && <p className="text-red-500 text-sm mt-1">{errors.price}</p>}
          </div>
        </div>

        {/* Obrázky - Upload & Preview */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">Obrázky</label>

          {/* Dropzone */}
          <div
            onDragOver={handleDragOver}
            onDrop={handleDrop}
            onClick={() => document.getElementById('image-upload').click()}
            className="border-2 border-dashed border-gray-300 rounded-lg p-6 text-center hover:border-blue-500 transition cursor-pointer bg-gray-50"
          >
            <input
              type="file"
              id="image-upload"
              multiple
              accept="image/*"
              onChange={handleImageSelect}
              className="hidden"
            />
            <p className="text-gray-500">Klikněte nebo přetáhněte obrázky sem</p>
          </div>

          {/* Náhledy */}
          {images.length > 0 && (
            <div className="mt-4 grid grid-cols-3 md:grid-cols-5 gap-4">
              {images.map((img, index) => (
                <div key={img.id} className="relative group aspect-square border rounded overflow-hidden">
                  <img src={img.preview} alt="Preview" className="w-full h-full object-cover" />

                  {/* Tlačítko smazat */}
                  <button
                    type="button"
                    onClick={(e) => {
                      e.stopPropagation();
                      handleRemoveImage(index);
                    }}
                    className="absolute top-1 right-1 bg-red-500 text-white rounded-full w-6 h-6 flex items-center justify-center text-xs opacity-80 hover:opacity-100"
                  >
                    ✕
                  </button>

                  {/* Štítek existujícího obrázku */}
                  {img.isExisting && (
                    <span className="absolute bottom-0 left-0 right-0 bg-black bg-opacity-50 text-white text-xs text-center py-1">
                      Uloženo
                    </span>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Tlačítka */}
        <div className="flex gap-4 pt-2">
          <button
            type="submit"
            disabled={isSubmitting}
            className="flex-1 bg-blue-600 text-white py-3 px-4 rounded-lg font-semibold hover:bg-blue-700 disabled:opacity-50 transition"
          >
            {isSubmitting ? 'Ukládám...' : (isEditing ? 'Uložit změny' : 'Vložit inzerát')}
          </button>

          <button
            type="button"
            onClick={onCancel}
            className="px-6 py-3 border border-gray-300 text-gray-700 rounded-lg font-semibold hover:bg-gray-100 transition"
          >
            Zrušit
          </button>
        </div>

      </form>
    </div>
  );
};

export default AdminAddProductForm;