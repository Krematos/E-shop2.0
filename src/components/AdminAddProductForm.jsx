import React, { useState, useEffect, useRef } from "react";
import { createProduct, updateProduct, uploadProductImages, updateMainImage, deleteProductImage, setExistingImageAsMain } from "../services/productService";
import { getImageUrl } from "../utils/urlUtils";
import { optimizeImage } from "../utils/imageOptimizer";

const MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB – sjednoceno s BE (ProductServiceImpl)

const getStatusErrorMessage = (status, data) => {
  if (status === 401 || status === 403) {
    return 'Nemáte oprávnění k této akci. Pravděpodobně vypršelo vaše přihlášení, zkuste se přihlásit znovu.';
  }
  if (status === 404) {
    return 'Požadovaný záznam nebyl nalezen (pravděpodobně byl již archivován nebo smazán).';
  }
  return data?.message || data?.error || 'Došlo k nečekané chybě na serveru. Zkuste to prosím později.';
};

const handleApiError = (error, setErrors) => {
  console.error("Chyba při ukládání:", error);

  if (!error.response) {
    setErrors({ submit: 'Nepodařilo se připojit k serveru. Zkontrolujte prosím své připojení k internetu.' });
    return;
  }

  const { status, data } = error.response;
  if (status === 400) {
    const backendErrors = typeof data === 'object' && data !== null ? data : {};
    const backendMessage = data?.message || data?.error || 'Zkontrolujte prosím zadaná data podle zvýrazněných chyb.';
    setErrors(prev => ({
      ...prev,
      submit: backendMessage,
      ...backendErrors
    }));
    return;
  }

  setErrors({ submit: getStatusErrorMessage(status, data) });
};

const uploadImagesForProduct = async (productId, images, mainImageIndex) => {
  const selectedMainImage = images[mainImageIndex];
  let newImagesToUpload = images.filter(img => !img.isExisting && img.file);

  // Pokud je jako hlavní vybrán NOVÝ obrázek (dosud nenahraný)
  if (selectedMainImage?.file && !selectedMainImage.isExisting) {
    // Nahraje ho přes dedikovaný endpoint
    await updateMainImage(productId, selectedMainImage.file);
    // Odfiltruje ho z těch, co se budou nahrávat jako "další"
    newImagesToUpload = newImagesToUpload.filter(img => img.id !== selectedMainImage.id);
  } else if (selectedMainImage?.isExisting && selectedMainImage.imageId && !selectedMainImage.isServerMain) {
    // Pokud je hlavní existující obrázek a ještě nebyl hlavní na serveru
    await setExistingImageAsMain(productId, selectedMainImage.imageId);
    selectedMainImage.isServerMain = true;
  }

  // Nahrát zbytek nových obrázků (pokud nějaké zbyly)
  if (newImagesToUpload.length > 0) {
    await uploadProductImages(productId, newImagesToUpload);
  }
};

const AdminAddProductForm = ({ initialData, onProductSaved, onCancel }) => {
  const isEditing = !!initialData;

  // -- STATE --
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    category: '',
    price: '',
    stockQuantity: '',
    vatRate: '21',
  });

  const [images, setImages] = useState([]);
  const [mainImageIndex, setMainImageIndex] = useState(0);
  const [errors, setErrors] = useState({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Ref pro uvolnění paměti (URL.revokeObjectURL) při unmountu komponenty
  const imagesRef = useRef(images);
  useEffect(() => {
    imagesRef.current = images;
  }, [images]);

  useEffect(() => {
    return () => {
      imagesRef.current.forEach(img => {
        if (!img.isExisting && img.preview) {
          URL.revokeObjectURL(img.preview);
        }
      });
    };
  }, []);

  // -- INITIALIZATION (Předvyplnění při editaci / reset při novém) --
  useEffect(() => {
    if (initialData) {
      let initialStock = '';
      if (initialData.totalStock !== undefined) {
        initialStock = String(initialData.totalStock);
      } else if (initialData.stockQuantity !== undefined) {
        initialStock = String(initialData.stockQuantity);
      } else if (initialData.availableStock !== undefined) {
        initialStock = String(initialData.availableStock);
      }

      setFormData({
        name: initialData.name || '',
        description: initialData.description || '',
        category: initialData.category || '',
        price: initialData.price !== undefined && initialData.price !== null ? String(initialData.price) : '',
        stockQuantity: initialStock,
        vatRate: initialData.vatRate !== undefined ? String(initialData.vatRate) : '21',
      });

      if (initialData.images && Array.isArray(initialData.images)) {
        const existingImages = initialData.images.map((imgObj, index) => ({
          file: null,
          preview: getImageUrl(imgObj.url),
          id: imgObj.id ? `existing-${imgObj.id}` : `existing-${index}-${imgObj.url}`,
          imageId: imgObj.id,
          isExisting: true,
          isServerMain: !!(imgObj.main || imgObj.isMain),
          filename: imgObj.url
        }));
        setImages(existingImages);

        const mIndex = initialData.images.findIndex(imgObj => imgObj.main || imgObj.isMain);
        if (mIndex !== -1) {
          setMainImageIndex(mIndex);
        } else {
          setMainImageIndex(0);
        }
      } else if (initialData.mainImageUrl) {
        setImages([{
          file: null,
          preview: getImageUrl(initialData.mainImageUrl),
          id: `existing-${initialData.mainImageUrl}`,
          imageId: null,
          isExisting: true,
          isServerMain: true,
          filename: initialData.mainImageUrl
        }]);
        setMainImageIndex(0);
      } else {
        setImages([]);
        setMainImageIndex(0);
      }
    } else {
      // Reset formuláře při přechodu do režimu nového produktu
      setFormData({
        name: '',
        description: '',
        category: '',
        price: '',
        stockQuantity: '',
        vatRate: '21',
      });
      setImages([]);
      setMainImageIndex(0);
      setErrors({});
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
    const files = Array.from(e.target.files).filter(f => f.type.startsWith('image/'));
    // Reset hodnoty inputu, aby šel vybrat stejný soubor znovu
    e.target.value = '';

    if (files.length === 0 && e.target.files && e.target.files.length > 0) {
      globalThis.alert("Vybrané soubory nejsou platné obrázky.");
      return;
    }
    processFiles(files).catch(err => {
      console.error("Chyba při zpracování souborů:", err);
    });
  };

  // Zpracování souborů (Drag&Drop i Input)
  const processFiles = async (files) => {
    const validFiles = [];
    let hasLargeFiles = false;

    files.forEach(file => {
      if (file.size > MAX_FILE_SIZE) {
        hasLargeFiles = true;
      } else {
        validFiles.push(file);
      }
    });

    if (hasLargeFiles) {
      globalThis.alert("Některé obrázky jsou příliš velké! Maximální velikost jednoho souboru je 5 MB. Tyto nadlimitní soubory byly přeskočeny.");
    }

    // Optimalizace a ořez obrázků (WebP, 4:3)
    const optimizedFiles = await Promise.all(
      validFiles.map(async (file) => {
        try {
          return await optimizeImage(file, 4 / 3, 1200, 0.8);
        } catch (error) {
          console.error("Chyba při optimalizaci obrázku:", error);
          return file; // Fallback
        }
      })
    );

    const newImages = optimizedFiles.map(file => ({
      file: file,
      preview: URL.createObjectURL(file),
      id: (typeof crypto !== 'undefined' && crypto.randomUUID) ? crypto.randomUUID() : `${Date.now()}-${Math.random()}`,
      isExisting: false,
      isServerMain: false
    }));

    setImages(prev => [...prev, ...newImages]);
  };

  const handleRemoveImage = async (index) => {
    const imgToRemove = images[index];
    if (!imgToRemove) return;

    if (imgToRemove.isExisting) {
      // Kontrola, zda se nepokouší smazat hlavní obrázek
      if (mainImageIndex === index) {
        globalThis.alert("Nelze smazat hlavní obrázek. Nejprve nastavte jako hlavní jiný obrázek.");
        return;
      }

      // Pokud je obrázek na serveru stále veden jako hlavní (i když uživatel na FE kliknul na jiný)
      if (imgToRemove.isServerMain) {
        const selectedMain = images[mainImageIndex];
        if (selectedMain?.isExisting && selectedMain.imageId) {
          // Před smazáním přepneme na serveru hlavní obrázek na jiný existující
          try {
            await setExistingImageAsMain(initialData.id, selectedMain.imageId);
            selectedMain.isServerMain = true;
            imgToRemove.isServerMain = false;
          } catch (error) {
            console.error("Chyba při přenastavení hlavního obrázku před smazáním:", error);
            globalThis.alert("Nepodařilo se změnit hlavní obrázek před smazáním původního.");
            return;
          }
        } else {
          globalThis.alert("Tento obrázek je na serveru nastaven jako hlavní. Nejprve nastavte a uložte jako hlavní jiný existující obrázek.");
          return;
        }
      }

      if (!globalThis.confirm("Opravdu chcete trvale smazat tento obrázek z produktu? Akce je nevratná.")) {
        return;
      }

      try {
        await deleteProductImage(initialData.id, imgToRemove.filename);
      } catch (error) {
        console.error("Chyba při mazání obrázku:", error);
        const msg = error.response?.data?.message || error.response?.data?.error || "";
        globalThis.alert("Nepodařilo se smazat obrázek. " + msg);
        return;
      }
    } else {
      // Uvolnění paměti prohlížeče pro lokální náhled
      if (imgToRemove.preview) {
        URL.revokeObjectURL(imgToRemove.preview);
      }
    }

    setImages(prev => {
      const updated = prev.filter((_, i) => i !== index);
      if (updated.length === 0 || mainImageIndex === index) {
        setMainImageIndex(0);
      } else if (mainImageIndex > index) {
        setMainImageIndex(prevIndex => prevIndex - 1);
      }
      return updated;
    });
  };

  // Drag & Drop Handlery
  const handleDragOver = (e) => { e.preventDefault(); };
  const handleDrop = (e) => {
    e.preventDefault();
    const files = Array.from(e.dataTransfer.files).filter(f => f.type.startsWith('image/'));
    if (files.length === 0 && e.dataTransfer.files.length > 0) {
      globalThis.alert("Přetažené soubory nejsou platné obrázky.");
      return;
    }
    processFiles(files).catch(err => {
      console.error("Chyba při zpracování přetažených souborů:", err);
    });
  };

  // Validace
  const validateForm = () => {
    const newErrors = {};

    const nameTrimmed = formData.name.trim();
    if (!nameTrimmed) {
      newErrors.name = 'Název je povinný';
    } else if (nameTrimmed.length < 2 || nameTrimmed.length > 100) {
      newErrors.name = 'Název musí mít 2 až 100 znaků';
    }

    const descTrimmed = formData.description.trim();
    if (!descTrimmed) {
      newErrors.description = 'Popis je povinný';
    } else if (descTrimmed.length > 1000) {
      newErrors.description = 'Popis nesmí překročit 1000 znaků';
    }

    if (!formData.category) {
      newErrors.category = 'Vyberte kategorii';
    }

    const numPrice = Number.parseFloat(formData.price);
    if (!formData.price || Number.isNaN(numPrice) || numPrice < 0.01) {
      newErrors.price = 'Cena musí být kladné číslo (min. 0.01 Kč)';
    }

    const numStock = Number(formData.stockQuantity);
    if (formData.stockQuantity === '' || Number.isNaN(numStock) || numStock < 0 || !Number.isInteger(numStock)) {
      newErrors.stockQuantity = 'Zadejte platné celé číslo (0 nebo více)';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleEditProduct = async () => {
    const updatePayload = {
      ...formData,
      price: Number.parseFloat(formData.price),
      stockQuantity: Number.parseInt(formData.stockQuantity, 10),
      mainImageIndex: mainImageIndex
    };

    // 1. Odeslat textová data
    await updateProduct(initialData.id, updatePayload);

    // 2. Nahrát nové fotky a aktualizovat hlavní obrázek
    await uploadImagesForProduct(initialData.id, images, mainImageIndex);
  };

  const handleCreateProduct = async () => {
    const productPayload = {
      ...formData,
      price: Number.parseFloat(formData.price),
      stockQuantity: Number.parseInt(formData.stockQuantity, 10),
      images: images,
      mainImageIndex: mainImageIndex
    };
    await createProduct(productPayload);
  };

  // Odeslání formuláře
  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validateForm()) return;

    setIsSubmitting(true);

    try {
      if (isEditing) {
        await handleEditProduct();
      } else {
        await handleCreateProduct();
      }

      // Úspěch
      if (onProductSaved) onProductSaved();
    } catch (error) {
      handleApiError(error, setErrors);
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
          <label htmlFor="product-name" className="block text-sm font-medium text-gray-700 mb-1">
            Název produktu
          </label>
          <input
            type="text"
            id="product-name"
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
          <label htmlFor="product-description" className="block text-sm font-medium text-gray-700 mb-1">
            Popis
          </label>
          <textarea
            id="product-description"
            name="description"
            value={formData.description}
            onChange={handleChange}
            rows={4}
            className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 ${errors.description ? 'border-red-500' : 'border-gray-300'}`}
          />
          {errors.description && <p className="text-red-500 text-sm mt-1">{errors.description}</p>}
        </div>

        {/* Kategorie, Cena, DPH a Množství skladem */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          <div>
            <label htmlFor="product-category" className="block text-sm font-medium text-gray-700 mb-1">
              Kategorie
            </label>
            <select
              id="product-category"
              name="category"
              value={formData.category}
              onChange={handleChange}
              className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 transition-shadow ${errors.category ? 'border-red-500' : 'border-gray-300'}`}
            >
              <option value="">Vyberte kategorii</option>
              <option value="Elektronika">Elektronika</option>
              <option value="Oblečení">Oblečení</option>
              <option value="Domácnost">Domácnost</option>
              <option value="Ostatní">Ostatní</option>
            </select>
            {errors.category && <p className="text-red-500 text-sm mt-1">{errors.category}</p>}
          </div>

          <div>
            <label htmlFor="product-price" className="block text-sm font-medium text-gray-700 mb-1">
              Cena (Kč)
            </label>
            <input
              type="number"
              id="product-price"
              name="price"
              value={formData.price}
              onChange={handleChange}
              min="0.01"
              step="0.01"
              className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 transition-shadow ${errors.price ? 'border-red-500' : 'border-gray-300'}`}
              placeholder="0.00"
            />
            {errors.price && <p className="text-red-500 text-sm mt-1">{errors.price}</p>}
          </div>

          <div>
            <label htmlFor="product-vat-rate" className="block text-sm font-medium text-gray-700 mb-1">
              Sazba DPH
            </label>
            <select
              id="product-vat-rate"
              name="vatRate"
              value={formData.vatRate}
              onChange={handleChange}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 transition-shadow"
            >
              <option value="21">21 %</option>
              <option value="12">12 %</option>
              <option value="0">0 %</option>
            </select>
          </div>

          <div>
            <label htmlFor="product-stock-quantity" className="block text-sm font-medium text-gray-700 mb-1">
              Skladem (ks)
            </label>
            <input
              type="number"
              id="product-stock-quantity"
              name="stockQuantity"
              value={formData.stockQuantity}
              onChange={handleChange}
              min="0"
              step="1"
              className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 transition-shadow ${errors.stockQuantity ? 'border-red-500' : 'border-gray-300'}`}
              placeholder="Např. 10"
            />
            {errors.stockQuantity && <p className="text-red-500 text-sm mt-1">{errors.stockQuantity}</p>}
          </div>
        </div>

        {/* Obrázky - Upload & Preview */}
        <div>
          <label htmlFor="image-upload" className="block text-sm font-medium text-gray-700 mb-2 cursor-pointer">
            Obrázky
          </label>

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
                <div key={img.id} className={`relative group aspect-square border-2 rounded overflow-hidden ${mainImageIndex === index ? 'border-yellow-500 shadow-md' : 'border-transparent'}`}>
                  <img src={img.preview} alt="Preview" className="w-full h-full object-cover" />

                  {/* Nastavit jako hlavní */}
                  <button
                    type="button"
                    onClick={(e) => {
                      e.preventDefault();
                      e.stopPropagation();
                      setMainImageIndex(index);
                    }}
                    className={`absolute top-1 left-1 ${mainImageIndex === index ? 'bg-yellow-500 text-white' : 'bg-gray-200 text-gray-700 bg-opacity-70'} rounded-full w-6 h-6 flex items-center justify-center text-xs hover:bg-yellow-500 hover:text-white transition`}
                    title="Nastavit jako hlavní obrázek"
                    aria-label={mainImageIndex === index ? "Aktuální hlavní obrázek" : "Nastavit jako hlavní obrázek"}
                  >
                    ⭐
                  </button>

                  {/* Tlačítko smazat */}
                  <button
                    type="button"
                    onClick={(e) => {
                      e.stopPropagation();
                      void handleRemoveImage(index).catch(err => {
                        console.error("Chyba při odstraňování obrázku:", err);
                      });
                    }}
                    className="absolute top-1 right-1 bg-red-500 text-white rounded-full w-6 h-6 flex items-center justify-center text-xs opacity-80 hover:opacity-100"
                    title="Smazat obrázek"
                    aria-label="Smazat obrázek"
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
