package vn.aequitas.coreplatform.application.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Generic page envelope, the counterpart of the .NET {@code PagedResult<T>}.
 *
 * @param <T> item type
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedResult<T> {

    @Builder.Default
    private List<T> items = new ArrayList<>();

    private int page;

    private int pageSize;

    private int totalCount;

    private int totalPages;
}
