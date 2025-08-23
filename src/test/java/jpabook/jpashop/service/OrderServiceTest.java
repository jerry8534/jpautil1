package jpabook.jpashop.service;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class OrderServiceTest {

    @Autowired
    EntityManager em;
    @Autowired
    OrderService orderService;
    @Autowired
    OrderRepository orderRepository;

    @Test
    public void createOrder() throws Exception {
        // given
        Member member = new Member();
        member.setName("member1");
        member.setAddress(new Address("seoul", "a", "123"));
        em.persist(member);

        Book book = createBook();

        int orderCount = 2;

        // when
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        // then
        Order order = orderRepository.findOne(orderId);

        assertEquals(OrderStatus.ORDER, order.getStatus(), "status is order");
        assertEquals(1, order.getOrderItems().size(), "items size must be precise");
        assertEquals(10000 * orderCount, order.getTotalPrice(), "order price is itemprice * count");
        assertEquals(8, book.getStockQuantity(), "stock must be reduced");
    }

    @Test
    public void orderStockOver() throws Exception {
        // given
        Member member = createMember();
        Item item = createBook();

        int orderCount = 11;

        // when
        orderService.order(member.getId(), item.getId(), orderCount);

        //then
        fail("must error by stock");
    }

    @Test
    public void cancelOrder() throws Exception {
        Member member = createMember();
        Book book = createBook();

        int orderCount = 2;

        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        // when
        orderService.cancelOrder(orderId);

        // then
        Order order = orderRepository.findOne(orderId);

        assertEquals(OrderStatus.CANCEL, order.getStatus(), "status is cancel");
        assertEquals(10, book.getStockQuantity(), "items size must be added");
    }

    private Book createBook() {
        Book book = new Book();
        book.setName("book1");
        book.setPrice(10000);
        book.setStockQuantity(10);
        em.persist(book);
        return book;
    }

    private Member createMember() {
        Member member = new Member();
        member.setName("member1");
        member.setAddress(new Address("seoul", "a", "123"));
        em.persist(member);
        return member;
    }
}