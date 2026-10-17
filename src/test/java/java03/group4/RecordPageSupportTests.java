package java03.group4;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

class RecordPageSupportTests {

    @Test
    void requestedPagePastTheEndIsClampedToTheLastPage() {
        List<Pageable> requestedPages = new ArrayList<>();

        Page<String> result = RecordPageSupport.load(
                pageable -> {
                    requestedPages.add(pageable);
                    return new PageImpl<>(List.of(), pageable, 25);
                },
                9,
                Sort.by("name"));

        assertEquals(2, requestedPages.size());
        assertEquals(9, requestedPages.get(0).getPageNumber());
        assertEquals(2, requestedPages.get(1).getPageNumber());
        assertEquals(2, result.getNumber());
        assertEquals(RecordPageSupport.PAGE_SIZE, requestedPages.get(0).getPageSize());
    }
}
