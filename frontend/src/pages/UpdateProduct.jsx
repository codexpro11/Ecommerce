import { useState, useEffect } from 'react';
import { useNavigate, useParams, Link } from 'react-router-dom';
import { getProductById, getProductImageUrl, updateProduct, generateDescription, generateImage } from '../api/client';
import './AddProduct.css';

export default function UpdateProduct() {
    const { id } = useParams();
    const navigate = useNavigate();
    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState(null);
    const [form, setForm] = useState({
        productName: '',
        price: '',
        stockQuantity: '',
        category: '',
        releaseDate: '',
        productAvailable: true,
        description: '',
        brand: '',
    });
    const [imageFile, setImageFile] = useState(null);
    const [existingImageUrl, setExistingImageUrl] = useState(null);
    const [descLoading, setDescLoading] = useState(false);
    const [imageLoading, setImageLoading] = useState(false);

    useEffect(() => {
        const fetchProduct = async () => {
            try {
                const product = await getProductById(id);
                setForm({
                    productName: product.productName || '',
                    price: product.price != null ? String(product.price) : '',
                    stockQuantity: product.stockQuantity != null ? String(product.stockQuantity) : '',
                    category: product.category || '',
                    releaseDate: product.releaseDate ? product.releaseDate.slice(0, 10) : '',
                    productAvailable: product.productAvailable ?? true,
                    description: product.description || '',
                    brand: product.brand || '',
                });
                if (product.imageName) {
                    setExistingImageUrl(getProductImageUrl(id));
                }
            } catch (e) {
                setError('Failed to load product: ' + e.message);
            } finally {
                setLoading(false);
            }
        };
        fetchProduct();
    }, [id]);

    const handleChange = (e) => {
        const { name, value, type, checked } = e.target;
        setForm((prev) => ({
            ...prev,
            [name]: type === 'checkbox' ? checked : value,
        }));
    };

    const handleGenerateDescription = async () => {
        if (!form.productName.trim()) {
            setError('Enter a product name first.');
            return;
        }
        setError(null);
        setDescLoading(true);
        try {
            const text = await generateDescription(form.productName.trim(), form.category.trim() || 'General');
            setForm((prev) => ({ ...prev, description: text }));
        } catch (e) {
            setError(e.message);
        } finally {
            setDescLoading(false);
        }
    };

    const handleGenerateImage = async () => {
        if (!form.productName.trim()) {
            setError('Enter a product name first.');
            return;
        }
        setError(null);
        setImageLoading(true);
        try {
            const blob = await generateImage(
                form.productName.trim(),
                form.description.trim() || form.productName,
                form.category.trim() || 'General'
            );
            const file = new File([blob], 'ai-generated.png', { type: blob.type || 'image/png' });
            setImageFile(file);
            setExistingImageUrl(null);
        } catch (e) {
            setError(e.message);
        } finally {
            setImageLoading(false);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError(null);
        setSubmitting(true);

        const product = {
            id: parseInt(id, 10),
            productName: form.productName.trim(),
            price: parseInt(form.price, 10) || 0,
            stockQuantity: form.stockQuantity === '' ? null : parseInt(form.stockQuantity, 10),
            category: form.category.trim() || null,
            releaseDate: form.releaseDate || null,
            productAvailable: form.productAvailable,
            description: form.description.trim() || null,
            brand: form.brand.trim() || null,
        };

        try {
            await updateProduct(id, product, imageFile || undefined);
            navigate('/products');
        } catch (e) {
            setError(e.message);
            setSubmitting(false);
        }
    };

    if (loading) {
        return (
            <div className="container add-product-page">
                <h1 className="page-title">Update Product</h1>
                <div className="card" style={{ textAlign: 'center', padding: '3rem' }}>
                    <p style={{ color: 'var(--text-secondary)' }}>Loading product details…</p>
                </div>
            </div>
        );
    }

    return (
        <div className="container add-product-page">
            <h1 className="page-title">Update Product</h1>
            <Link to={`/product/${id}`} className="back-link">← Back to product</Link>

            <form className="add-product-form card" onSubmit={handleSubmit}>
                <div className="form-row">
                    <div className="form-group">
                        <label htmlFor="productName">Product name *</label>
                        <input
                            id="productName"
                            name="productName"
                            type="text"
                            required
                            value={form.productName}
                            onChange={handleChange}
                            placeholder="e.g. Vitamin C Serum"
                        />
                    </div>
                    <div className="form-group">
                        <label htmlFor="brand">Brand</label>
                        <input
                            id="brand"
                            name="brand"
                            type="text"
                            value={form.brand}
                            onChange={handleChange}
                            placeholder="e.g. Minimalist"
                        />
                    </div>
                </div>

                <div className="form-row">
                    <div className="form-group">
                        <label htmlFor="price">Price (₹) *</label>
                        <input
                            id="price"
                            name="price"
                            type="number"
                            min="0"
                            required
                            value={form.price}
                            onChange={handleChange}
                            placeholder="649"
                        />
                    </div>
                    <div className="form-group">
                        <label htmlFor="stockQuantity">Stock quantity</label>
                        <input
                            id="stockQuantity"
                            name="stockQuantity"
                            type="number"
                            min="0"
                            value={form.stockQuantity}
                            onChange={handleChange}
                            placeholder="21"
                        />
                    </div>
                    <div className="form-group">
                        <label htmlFor="category">Category</label>
                        <input
                            id="category"
                            name="category"
                            type="text"
                            value={form.category}
                            onChange={handleChange}
                            placeholder="e.g. cosmatic"
                        />
                    </div>
                </div>

                <div className="form-row">
                    <div className="form-group">
                        <label htmlFor="releaseDate">Release date</label>
                        <input
                            id="releaseDate"
                            name="releaseDate"
                            type="date"
                            value={form.releaseDate}
                            onChange={handleChange}
                        />
                    </div>
                    <div className="form-group checkbox-group">
                        <label>
                            <input
                                name="productAvailable"
                                type="checkbox"
                                checked={form.productAvailable}
                                onChange={handleChange}
                            />
                            Available for sale
                        </label>
                    </div>
                </div>

                <div className="form-group">
                    <label htmlFor="description">Description</label>
                    <div className="form-group-with-action">
                        <textarea
                            id="description"
                            name="description"
                            rows={3}
                            value={form.description}
                            onChange={handleChange}
                            placeholder="e.g. Brightening serum"
                        />
                        <button
                            type="button"
                            className="btn btn-secondary btn-sm ai-btn"
                            onClick={handleGenerateDescription}
                            disabled={descLoading}
                        >
                            {descLoading ? 'Generating…' : '✨ Generate with AI'}
                        </button>
                    </div>
                </div>

                <div className="form-group">
                    <label htmlFor="imageFile">Product image</label>
                    {(existingImageUrl || imageFile) && (
                        <div style={{ marginBottom: '0.75rem' }}>
                            <img
                                src={imageFile ? URL.createObjectURL(imageFile) : existingImageUrl}
                                alt={form.productName}
                                style={{
                                    height: '150px',
                                    objectFit: 'contain',
                                    borderRadius: '8px',
                                    border: '1px solid var(--border)',
                                }}
                            />
                            <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginTop: '0.25rem' }}>
                                {imageFile ? imageFile.name : 'Current image — upload new to replace'}
                            </p>
                        </div>
                    )}
                    <div className="form-group-with-action">
                        <input
                            id="imageFile"
                            name="imageFile"
                            type="file"
                            accept="image/*"
                            onChange={(e) => {
                                const file = e.target.files?.[0] || null;
                                setImageFile(file);
                                if (file) setExistingImageUrl(null);
                            }}
                        />
                        <button
                            type="button"
                            className="btn btn-secondary btn-sm ai-btn"
                            onClick={handleGenerateImage}
                            disabled={imageLoading}
                        >
                            {imageLoading ? 'Generating…' : '🖼️ Generate image with AI'}
                        </button>
                    </div>
                </div>

                {error && <p className="form-error">{error}</p>}
                <div style={{ display: 'flex', gap: '1rem' }}>
                    <button type="submit" className="btn btn-primary" disabled={submitting}>
                        {submitting ? 'Updating…' : 'Update product'}
                    </button>
                    <button type="button" className="btn btn-secondary" onClick={() => navigate(`/product/${id}`)}>
                        Cancel
                    </button>
                </div>
            </form>
        </div>
    );
}
