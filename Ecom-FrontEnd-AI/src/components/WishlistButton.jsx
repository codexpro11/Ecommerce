import React, { useState, useEffect } from "react";
import { Heart } from "lucide-react";

const WishlistButton = ({ product }) => {
  const baseUrl = import.meta.env.VITE_BASE_URL;
  const [isLiked, setIsLiked] = useState(false);
  const [loading, setLoading] = useState(false);

  // Check if product is already in wishlist on component mount
  useEffect(() => {
    checkWishlistStatus();
  }, [product.id]);

  const checkWishlistStatus = async () => {
    try {
      const response = await fetch(`${baseUrl}/api/wishlist/check/${product.id}`);
      if (response.ok) {
        const isInWishlist = await response.json();
        setIsLiked(isInWishlist);
      }
    } catch (error) {
      console.error("Error checking wishlist status:", error);
    }
  };

  const handleToggle = async (e) => {
    // Prevent clicking the heart from opening the Product Details page
    e.stopPropagation();
    e.preventDefault();

    if (loading) return;
    setLoading(true);

    // Optimistic UI: Flip color immediately before server responds
    const previousState = isLiked;
    setIsLiked(!previousState);

    try {
      // ✅ FIXED: Removed trailing slash from URL
      const response = await fetch(`${baseUrl}/api/wishlist/toggle`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        // ✅ FIXED: Correct field mapping
        body: JSON.stringify({
          productId: product.id,              // Product ID
          productName: product.productName,   // Product Name
          price: product.price                // ✅ FIXED: Was product.productId, now product.price
        }),
      });

      if (!response.ok) throw new Error("Failed");

      const status = await response.text();
      // Sync state with server response to be sure
      if (status === "added") setIsLiked(true);
      if (status === "removed") setIsLiked(false);

    } catch (error) {
      console.error("Wishlist error:", error);
      setIsLiked(previousState); // Revert if server failed
    } finally {
      setLoading(false);
    }
  };

  return (
    <button
      onClick={handleToggle}
      className="btn btn-outline-danger d-flex align-items-center justify-content-center"
      style={{
        minWidth: '48px',
        height: '48px',
        border: '2px solid',
        borderColor: isLiked ? '#ef4444' : '#dee2e6',
        backgroundColor: isLiked ? '#fef2f2' : '#ffffff',
        transition: 'all 0.2s ease'
      }}
      title={isLiked ? "Remove from wishlist" : "Add to wishlist"}
      disabled={loading}
    >
      <Heart
        size={24}
        color={isLiked ? "#ef4444" : "#6c757d"} // Red if liked, gray if not
        fill={isLiked ? "#ef4444" : "none"}     // Filled if liked
        strokeWidth={2}
      />
    </button>
  );
};

export default WishlistButton;