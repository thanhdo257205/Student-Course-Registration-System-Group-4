package java03.group4;

import java.util.function.Function;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class RecordPageSupport {

    public static final int PAGE_SIZE = 10;

    private RecordPageSupport() {
    }

    public static <T> Page<T> load(
            Function<Pageable, Page<T>> pageQuery, int requestedPage, Sort sort) {
        int pageNumber = Math.max(0, requestedPage);
        Pageable pageable = PageRequest.of(pageNumber, PAGE_SIZE, sort);
        Page<T> result = pageQuery.apply(pageable);

        if (result.getTotalPages() > 0 && pageNumber >= result.getTotalPages()) {
            Pageable lastPage = PageRequest.of(result.getTotalPages() - 1, PAGE_SIZE, sort);
            return pageQuery.apply(lastPage);
        }
        return result;
    }
}
