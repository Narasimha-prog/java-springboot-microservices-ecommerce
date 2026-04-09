package com.lnreddy.cart_service.util;

import com.lnreddy.cart_service.dto.PageResponse;
import org.springframework.data.domain.Page;
import java.util.List;
import java.util.function.Function;

public class PagedUtils {

    /**
     * Generic static utility to map a Spring Data Page to a custom PageResponse.
     * * @param page           The source Spring Data Page (defines 'S')
     * @param mapperFunction The mapping logic (defines 'T' based on return type)
     * @param <S>            Source type (Entity)
     * @param <T>            Target type (DTO)
     * @return A populated PageResponse<T>
     */
    public static <S, T> PageResponse<T> toPageResponse(Page<S> page, Function<S, T> mapperFunction) {
        List<T> content = page.getContent().stream()
                .map(mapperFunction)
                .toList();

        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
