import React, { useEffect, useState } from "react";
import { useLocation, useNavigate, useSearchParams } from "react-router-dom";
import { toast } from "react-toastify";
import axios from "axios";

const SearchResults = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams(); // ✅ Fixed: properly get searchParams
  const query = searchParams.get("q");
  const [searchData, setSearchData] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchSearchResults = async () => {
      // If query is from URL params
      if (query) {
        try {
          setLoading(true);
          // ✅ Call your Spring Boot API
          const response = await axios.get(`http://localhost:8080/api/product/search-results`, {
            params: { query: query }
          });
          setSearchData(response.data);
        } catch (error) {
          console.error("Error fetching search results:", error);
          toast.error("Failed to fetch search results");
          setSearchData([]);
        } finally {
          setLoading(false);
        }
      } 
      // If search data is passed via navigation state (optional fallback)
      else if (location.state && location.state.searchData) {
        setSearchData(location.state.searchData);
        setLoading(false);
      } 
      // No query or data available
      else {
        toast.warning("Please enter a search term");
        navigate("/");
      }
    };

    fetchSearchResults();
  }, [query, location, navigate]);

  // Function to convert base64 string to data URL
  const convertBase64ToDataURL = (base64String, mimeType = 'image/jpeg') => {
    if (!base64String) return '/placeholder-image.jpg'; // ✅ Fixed: use proper fallback
    
    // If it's already a data URL, return as is
    if (base64String.startsWith('data:')) {
      return base64String;
    }
    
    // If it's already a URL, return as is
    if (base64String.startsWith('http')) {
      return base64String;
    }
    
    // Convert base64 string to data URL
    return `data:${mimeType};base64,${base64String}`;
  };

  const handleViewProduct = (productId) => {
    navigate(`/product/${productId}`);
  };

  const handleAddToCart = (productId) => {
    toast.success(`Product with ID ${productId} added to cart!`);
    // Add your cart logic here
  };

  if (loading) {
    return (
      <div className="container mt-5 pt-5 d-flex justify-content-center align-items-center" style={{ minHeight: "50vh" }}>
        <div className="spinner-border text-primary" role="status">
          <span className="visually-hidden">Loading...</span>
        </div>
      </div>
    );
  }

  return (
    <div className="container mt-5 pt-5">
      <h2 className="mb-4">
        Search Results {query && <span className="text-muted">for "{query}"</span>}
      </h2>
      
      {searchData.length === 0 ? (
        <div className="alert alert-info">
          <i className="bi bi-info-circle-fill me-2"></i>
          No products found matching your search criteria.
        </div>
      ) : (
        <>
          <p className="text-muted mb-4">{searchData.length} product(s) found</p>
          
          <div className="row row-cols-1 row-cols-sm-2 row-cols-md-3 row-cols-lg-4 g-4">
            {searchData.map((product) => (
              <div key={product.id} className="col">
                <div className="card h-100 shadow-sm">
                  <img 
                    src={convertBase64ToDataURL(product.product)}
                    className="card-img-top p-3" 
                    alt={product.productName}
                    style={{ height: "200px", objectFit: "contain", cursor: "pointer" }}
                    onClick={() => handleViewProduct(product.id)}
                  />
                  <div className="card-body d-flex flex-column">
                    <h5 className="card-title">{product.productName}</h5>
                    <p className="card-text text-muted mb-1">{product.brand}</p>
                    <div className="mb-2">
                      <span className="badge bg-secondary">{product.category}</span>
                    </div>
                    <p className="card-text small">
                      {product.description && product.description.length > 100
                        ? product.description.substring(0, 100) + "..."
                        : product.description || "No description available"}
                    </p>
                    <h5 className="card-text text-primary mt-auto mb-3">₹{product.price.toLocaleString('en-IN')}</h5>
                    <div className="d-flex justify-content-between mt-auto">
                      <button 
                        className="btn btn-outline-primary btn-sm"
                        onClick={() => handleViewProduct(product.id)}
                      >
                        View Details
                      </button>
                      <button 
                        className="btn btn-primary btn-sm"
                        onClick={() => handleAddToCart(product.id)}
                        disabled={!product.productAvailable || product.stockQuantity <= 0}
                      >
                        {product.productAvailable && product.stockQuantity > 0
                          ? "Add to Cart"
                          : "Out of Stock"}
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </>
      )}
    </div>
  );
};

export default SearchResults;