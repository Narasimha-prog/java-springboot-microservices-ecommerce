package com.lnreddy.cart_service.mapper;

import com.eswar.grpc.cart.CartItem;
import com.lnreddy.cart_service.dto.CartItemDTO;
import com.lnreddy.cart_service.dto.CartItemRequest;
import com.lnreddy.cart_service.dto.CartResponseDTO;
import com.lnreddy.cart_service.entity.CartEntity;
import com.lnreddy.cart_service.entity.CartItemEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface ICartMapper {

    /**
     * Maps the core Cart details along with the ENRICHED items and total.
     * We pass the enriched Set<CartItemDTO> because MapStruct cannot
     * call gRPC services during the mapping process.
     */
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "items", source = "enrichedItems")
    @Mapping(target = "totalAmount", source = "total")
    CartResponseDTO toResponseDTO(String userId, Set<CartItemDTO> enrichedItems, BigDecimal total);

    /**
     * Maps the incoming Request to the Database Entity.
     * We ignore 'id' and 'cart' because they are handled by JPA/Service.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cart", ignore = true)
    @Mapping(target = "productId", source = "request.productId")
    @Mapping(target = "quantity", source = "request.quantity")
    CartItemEntity toEntity(CartItemRequest request);
}
