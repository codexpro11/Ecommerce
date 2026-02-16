import React, { useEffect, useState, useContext } from "react";
import axios from "axios"; // or use your custom axios instance
import { Link } from "react-router-dom";
import { Trash2, ShoppingCart } from "lucide-react";
import AppContext from "../Context/Context"; // Import if you want to use addToCart

const Wishlist = () => {
  const [items, setItems] = useState([]);
  const { addToCart } = useContext(AppContext); // Optional: if you want 'Add to Cart' functionality
  const baseUrl = import.meta.env.VITE_BASE_URL;

  // Fetch Wishlist Data
  useEffect(() => {
    loadWishlist();
  }, []);

  const loadWishlist = async () => {
    try {
      const result = await axios.get(`${baseUrl}/api/wishlist/`);
      setItems(result.data);
    } catch (error) {
      console.error("Error loading wishlist:", error);
    }
  };

  // Remove Item
  const removeFromWishlist = async (id) => {
    try {
      // Note: We use the Wishlist Row ID (item.id) here, not productId
      await axios.delete(`${baseUrl}/api/wishlist/remove/${id}`);
      // Refresh list after delete
      loadWishlist();
    } catch (error) {
      console.error("Error removing item:", error);
    }
  };

  if (items.length === 0) {
    return (
      <div className="container mt-5 text-center">
        <h3>Your Wishlist is empty</h3>
        <Link to="/" className="btn btn-primary mt-3">Continue Shopping</Link>
      </div>
    );
  }

  return (
    <div className="container mt-5 pt-5">
      <h2 className="mb-4">My Wishlist</h2>
      <div className="row">
        {items.map((item) => (
          <div className="col-md-4 mb-4" key={item.id}>
            <div className="card h-100 shadow-sm">
              {/* Product Image - Fetched using productId */}
              <img
                src={`${baseUrl}/api/product/${item.productId}/image`}
                className="card-img-top"
                alt={item.productName}
                style={{ height: "200px", objectFit: "contain", padding: "10px" }}
              />

              <div className="card-body">
                <h5 className="card-title">{item.productName}</h5>
                <p className="card-text fw-bold">₹ {item.price}</p>

                <div className="d-flex gap-2">
                  {/* Add to Cart Button */}
                  <button
                    className="btn btn-primary flex-grow-1 d-flex align-items-center justify-content-center gap-2"
                    onClick={() => addToCart(item)} // Adjust logic if your context expects a full Product object
                  >
                    <ShoppingCart size={18} /> Add
                  </button>

                  {/* Remove Button */}
                  <button
                    className="btn btn-outline-danger"
                    onClick={() => removeFromWishlist(item.id)}
                    title="Remove"
                  >
                    <Trash2 size={18} />
                  </button>
                </div>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default Wishlist;