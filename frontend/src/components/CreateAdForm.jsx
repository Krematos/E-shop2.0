import { useState, useEffect } from 'react';
import { createAd } from '../services/adService';

const CreateAdForm = () => {
  // Form state
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    category: '',
    price: '',
    currency: 'Kč',
    termsAccepted: false,
  });

  // Images state
  const [images, setImages] = useState([]);
  const [mainImageIndex, setMainImageIndex] = useState(0);
  const [draggedIndex, setDraggedIndex] = useState(null);

  // Validation errors
  const [errors, setErrors] = useState({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitSuccess, setSubmitSuccess] = useState(false);
  const [uploadProgress, setUploadProgress] = useState(0);

  // Auto-save to localStorage
  useEffect(() => {
    const draft = localStorage.getItem('adDraft');
    if (draft) {
      try {
        const parsedDraft = JSON.parse(draft);
        setFormData(parsedDraft.formData || formData);
        if (parsedDraft.images) {
          // Reconstruct images from base64
          const imageObjects = parsedDraft.images.map((img, index) => ({
            file: null,
            preview: img,
            id: Date.now() + index,
          }));
          setImages(imageObjects);
        }
      } catch (error) {
        console.error('Chyba při načítání draftu:', error);
      }
    }
  }, []);

  // Auto-save on change
  useEffect(() => {
    const timer = setTimeout(() => {
      const draft = {
        formData,
        images: images.map(img => img.preview),
      };
      localStorage.setItem('adDraft', JSON.stringify(draft));
    }, 1000);

    return () => clearTimeout(timer);
  }, [formData, images]);

  // Handle input change
  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value,
    }));
    
    // Clear error when user starts typing
    if (errors[name]) {
      setErrors(prev => {
        const newErrors = { ...prev };
        delete newErrors[name];
        return newErrors;
      });
    }
  };

  // Handle image selection
  const handleImageSelect = (e) => {
    const files = Array.from(e.target.files);
    handleImages(files);
  };

  // Handle drag and drop
  const handleDragOver = (e) => {
    e.preventDefault();
    e.stopPropagation();
  };

  const handleDrop = (e) => {
    e.preventDefault();
    e.stopPropagation();
    const files = Array.from(e.dataTransfer.files).filter(file =>
      file.type.startsWith('image/')
    );
    handleImages(files);
  };

  // Process images
  const handleImages = (files) => {
    const newImages = files.map(file => {
      const reader = new FileReader();
      return new Promise((resolve) => {
        reader.onloadend = () => {
          resolve({
            file,
            preview: reader.result,
            id: Date.now() + Math.random(),
          });
        };
        reader.readAsDataURL(file);
      });
    });

    Promise.all(newImages).then(imageObjects => {
      setImages(prev => [...prev, ...imageObjects]);
    });
  };

  // Remove image
  const handleRemoveImage = (index) => {
    setImages(prev => {
      const newImages = prev.filter((_, i) => i !== index);
      if (mainImageIndex === index && newImages.length > 0) {
        setMainImageIndex(0);
      } else if (mainImageIndex > index) {
        setMainImageIndex(mainImageIndex - 1);
      }
      return newImages;
    });
  };

  // Set main image
  const handleSetMainImage = (index) => {
    setMainImageIndex(index);
  };

  // Drag and drop reorder
  const handleDragStart = (index) => {
    setDraggedIndex(index);
  };

  const handleDragEnd = () => {
    setDraggedIndex(null);
  };

  const handleDragEnter = (index) => {
    if (draggedIndex === null || draggedIndex === index) return;
    
    setImages(prev => {
      const newImages = [...prev];
      const draggedItem = newImages[draggedIndex];
      newImages.splice(draggedIndex, 1);
      newImages.splice(index, 0, draggedItem);
      if (mainImageIndex === draggedIndex) {
        setMainImageIndex(index);
      } else if (mainImageIndex === index) {
        setMainImageIndex(draggedIndex);
      } else if (mainImageIndex > draggedIndex && mainImageIndex <= index) {
        setMainImageIndex(mainImageIndex - 1);
      } else if (mainImageIndex < draggedIndex && mainImageIndex >= index) {
        setMainImageIndex(mainImageIndex + 1);
      }
      return newImages;
    });
    setDraggedIndex(index);
  };

  // Validation
  const validateForm = () => {
    const newErrors = {};

    // Title
    if (!formData.title.trim()) {
      newErrors.title = 'Název inzerátu je povinný';
    } else if (formData.title.trim().length < 5) {
      newErrors.title = 'Název musí mít alespoň 5 znaků';
    }

    // Description
    if (!formData.description.trim()) {
      newErrors.description = 'Popis je povinný';
    } else if (formData.description.trim().length < 20) {
      newErrors.description = 'Popis musí mít alespoň 20 znaků';
    }

    // Category
    if (!formData.category) {
      newErrors.category = 'Vyberte kategorii';
    }

    // Price
    if (formData.offerType === 'Prodej') {
      if (!formData.price || formData.price <= 0) {
        newErrors.price = 'Cena musí být větší než 0';
      }
    }

    // Email
    if (!formData.email.trim()) {
      newErrors.email = 'E-mail je povinný';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newErrors.email = 'Zadejte platnou e-mailovou adresu';
    }

    // Phone
    if (!formData.phone.trim()) {
      newErrors.phone = 'Telefon je povinný';
    } else if (!/^[\d\s\+\-\(\)]+$/.test(formData.phone)) {
      newErrors.phone = 'Zadejte platné telefonní číslo';
    }

    // Terms
    if (!formData.termsAccepted) {
      newErrors.termsAccepted = 'Musíte souhlasit s podmínkami';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  // Handle submit
  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validateForm()) {
      return;
    }

    setIsSubmitting(true);
    setUploadProgress(0);

    try {
      // Submit to API
      await createAd({
        ...formData,
        images: images,
        mainImageIndex: mainImageIndex,
        onProgress: (progress) => {
          setUploadProgress(progress);
        },
      });

      setUploadProgress(100);

      // Success
      setSubmitSuccess(true);
      
      // Clear form and draft
      setFormData({
        title: '',
        description: '',
        category: '',
        price: '',
        currency: 'Kč',
        termsAccepted: false,
      });
      setImages([]);
      setMainImageIndex(0);
      localStorage.removeItem('adDraft');

      // Reset success message after 5 seconds
      setTimeout(() => {
        setSubmitSuccess(false);
      }, 5000);

    } catch (error) {
      console.error('Chyba při odesílání inzerátu:', error);
      setErrors({
        submit: error.response?.data?.message || 'Chyba při odesílání inzerátu. Zkuste to znovu.',
      });
    } finally {
      setIsSubmitting(false);
      setUploadProgress(0);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 py-8 px-4">
      <div className="max-w-4xl mx-auto">
        <div className="bg-white rounded-lg shadow-md p-6 md:p-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-6">
            Vložit nový inzerát
          </h1>

          {submitSuccess && (
            <div className="mb-6 p-4 bg-green-50 border border-green-200 rounded-lg">
              <p className="text-green-800 font-semibold flex items-center">
                <span className="mr-2">✅</span>
                Inzerát byl úspěšně vložen!
              </p>
            </div>
          )}

          {errors.submit && (
            <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg">
              <p className="text-red-800">{errors.submit}</p>
            </div>
          )}

          {isSubmitting && uploadProgress > 0 && (
            <div className="mb-6">
              <div className="flex justify-between text-sm text-gray-600 mb-2">
                <span>Nahrávám...</span>
                <span>{uploadProgress}%</span>
              </div>
              <div className="w-full bg-gray-200 rounded-full h-2">
                <div
                  className="bg-blue-600 h-2 rounded-full transition-all duration-300"
                  style={{ width: `${uploadProgress}%` }}
                ></div>
              </div>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-6">
            {/* Title */}
            <div>
              <label htmlFor="title" className="block text-sm font-medium text-gray-700 mb-2">
                Název inzerátu <span className="text-red-500">*</span>
              </label>
              <input
                type="text"
                id="title"
                name="title"
                value={formData.title}
                onChange={handleChange}
                className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
                  errors.title ? 'border-red-500' : 'border-gray-300'
                }`}
                placeholder="Např. Prodám iPhone 13 Pro"
              />
              {errors.title && (
                <p className="mt-1 text-sm text-red-600">{errors.title}</p>
              )}
            </div>

            {/* Description */}
            <div>
              <label htmlFor="description" className="block text-sm font-medium text-gray-700 mb-2">
                Popis <span className="text-red-500">*</span>
              </label>
              <textarea
                id="description"
                name="description"
                value={formData.description}
                onChange={handleChange}
                rows={6}
                className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
                  errors.description ? 'border-red-500' : 'border-gray-300'
                }`}
                placeholder="Podrobný popis produktu..."
              />
              {errors.description && (
                <p className="mt-1 text-sm text-red-600">{errors.description}</p>
              )}
            </div>

            {/* Category and Offer Type */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label htmlFor="category" className="block text-sm font-medium text-gray-700 mb-2">
                  Kategorie <span className="text-red-500">*</span>
                </label>
                <select
                  id="category"
                  name="category"
                  value={formData.category}
                  onChange={handleChange}
                  className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
                    errors.category ? 'border-red-500' : 'border-gray-300'
                  }`}
                >
                  <option value="">Vyberte kategorii</option>
                  <option value="Elektronika">Elektronika</option>
                  <option value="Domácnost">Domácnost</option>
                  <option value="Oblečení">Oblečení</option>
                  <option value="Ostatní">Ostatní</option>
                </select>
                {errors.category && (
                  <p className="mt-1 text-sm text-red-600">{errors.category}</p>
                )}
              </div>


            </div>

            {/* Price and Currency */}
            {formData.offerType === 'Prodej' && (
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label htmlFor="price" className="block text-sm font-medium text-gray-700 mb-2">
                    Cena <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="number"
                    id="price"
                    name="price"
                    value={formData.price}
                    onChange={handleChange}
                    min="0"
                    step="0.01"
                    className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
                      errors.price ? 'border-red-500' : 'border-gray-300'
                    }`}
                    placeholder="0"
                  />
                  {errors.price && (
                    <p className="mt-1 text-sm text-red-600">{errors.price}</p>
                  )}
                </div>

                <div>
                  <label htmlFor="currency" className="block text-sm font-medium text-gray-700 mb-2">
                    Měna
                  </label>
                  <select
                    id="currency"
                    name="currency"
                    value={formData.currency}
                    onChange={handleChange}
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  >
                    <option value="Kč">Kč</option>
                    <option value="EUR">EUR</option>
                  </select>
                </div>
              </div>
            )}

            {/* Images */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Obrázky {images.length > 0 && <span className="text-gray-500">({images.length})</span>}
              </label>
              
              {/* Drag and Drop Area */}
              <div
                onDragOver={handleDragOver}
                onDrop={handleDrop}
                className="border-2 border-dashed border-gray-300 rounded-lg p-8 text-center hover:border-blue-400 transition-colors cursor-pointer"
                onClick={() => document.getElementById('image-upload').click()}
              >
                <input
                  type="file"
                  id="image-upload"
                  multiple
                  accept="image/*"
                  onChange={handleImageSelect}
                  className="hidden"
                />
                <div className="space-y-2">
                  <svg
                    className="mx-auto h-12 w-12 text-gray-400"
                    stroke="currentColor"
                    fill="none"
                    viewBox="0 0 48 48"
                  >
                    <path
                      d="M28 8H12a4 4 0 00-4 4v20m32-12v8m0 0v8a4 4 0 01-4 4H12a4 4 0 01-4-4v-4m32-4l-3.172-3.172a4 4 0 00-5.656 0L28 28M8 32l9.172-9.172a4 4 0 015.656 0L28 28m0 0l4 4m4-24h8m-4-4v8m-12 4h.02"
                      strokeWidth={2}
                      strokeLinecap="round"
                      strokeLinejoin="round"
                    />
                  </svg>
                  <p className="text-sm text-gray-600">
                    Klikněte nebo přetáhněte obrázky sem
                  </p>
                  <p className="text-xs text-gray-500">
                    Podporované formáty: JPG, PNG, GIF
                  </p>
                </div>
              </div>

              {/* Image Thumbnails */}
              {images.length > 0 && (
                <div className="mt-4 grid grid-cols-2 md:grid-cols-4 gap-4">
                  {images.map((image, index) => (
                    <div
                      key={image.id}
                      draggable
                      onDragStart={() => handleDragStart(index)}
                      onDragEnd={handleDragEnd}
                      onDragEnter={() => handleDragEnter(index)}
                      className={`relative group border-2 rounded-lg overflow-hidden ${
                        mainImageIndex === index
                          ? 'border-blue-500 ring-2 ring-blue-200'
                          : 'border-gray-200'
                      } ${
                        draggedIndex === index ? 'opacity-50' : ''
                      }`}
                    >
                      <img
                        src={image.preview}
                        alt={`Preview ${index + 1}`}
                        className="w-full h-32 object-cover"
                      />
                      {mainImageIndex === index && (
                        <div className="absolute top-2 left-2 bg-blue-500 text-white text-xs px-2 py-1 rounded">
                          Hlavní
                        </div>
                      )}
                      <div className="absolute inset-0 bg-black bg-opacity-0 group-hover:bg-opacity-50 transition-all flex items-center justify-center gap-2">
                        <button
                          type="button"
                          onClick={(e) => {
                            e.stopPropagation();
                            handleSetMainImage(index);
                          }}
                          className="opacity-0 group-hover:opacity-100 bg-blue-500 text-white px-2 py-1 rounded text-xs hover:bg-blue-600"
                        >
                          Hlavní
                        </button>
                        <button
                          type="button"
                          onClick={(e) => {
                            e.stopPropagation();
                            handleRemoveImage(index);
                          }}
                          className="opacity-0 group-hover:opacity-100 bg-red-500 text-white px-2 py-1 rounded text-xs hover:bg-red-600"
                        >
                          Smazat
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {/* Terms */}
            <div>
              <label className="flex items-start">
                <input
                  type="checkbox"
                  name="termsAccepted"
                  checked={formData.termsAccepted}
                  onChange={handleChange}
                  className="mt-1 mr-2 text-blue-600 focus:ring-blue-500"
                />
                <span className="text-sm text-gray-700">
                  Souhlasím s podmínkami používání <span className="text-red-500">*</span>
                </span>
              </label>
              {errors.termsAccepted && (
                <p className="mt-1 text-sm text-red-600">{errors.termsAccepted}</p>
              )}
            </div>

            {/* Submit Button */}
            <div className="flex gap-4">
              <button
                type="submit"
                disabled={isSubmitting}
                className="flex-1 bg-blue-600 text-white px-6 py-3 rounded-lg font-semibold hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
              >
                {isSubmitting ? 'Odesílám...' : 'Vložit inzerát'}
              </button>
              <button
                type="button"
                onClick={() => {
                  setFormData({
                    title: '',
                    description: '',
                    category: '',
                    price: '',
                    currency: 'Kč',
                    email: '',
                    termsAccepted: false,
                  });
                  setImages([]);
                  setErrors({});
                  localStorage.removeItem('adDraft');
                }}
                className="px-6 py-3 border border-gray-300 text-gray-700 rounded-lg font-semibold hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-offset-2 transition-colors"
              >
                Zrušit
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default CreateAdForm;

