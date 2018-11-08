package com.jeemyeong.coffeeservice

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit4.SpringRunner
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Duration

@RunWith(SpringRunner::class)
@WebFluxTest(CoffeeService::class)
class CoffeeServiceTest {
    @Autowired
    private lateinit var service: CoffeeService

    @MockBean
    private lateinit var repo: CoffeeRepo

    private val coffee1 = Coffee("000-111", "Tester's Choice")
    private val coffee2 = Coffee("000-222", "Maxfail House")

    @Before
    fun setUp() {
        Mockito.`when`(repo.findAll()).thenReturn(Flux.just(coffee1, coffee2))
        Mockito.`when`(repo.findById(coffee1.id!!)).thenReturn(Mono.just(coffee1))
        Mockito.`when`(repo.findById(coffee2.id!!)).thenReturn(Mono.just(coffee2))
    }

    @Test
    fun `Get all coffees`() {
        StepVerifier.create(service.getAllCoffees())
            .expectNext(coffee1)
            .expectNext(coffee2)
            .verifyComplete()
    }

    @Test
    fun `Get a coffee`() {
        StepVerifier.create(service.getCoffeeById(coffee1.id!!))
            .expectNext(coffee1)
            .verifyComplete()
    }

    @Test
    fun `Get orders for coffee 2, take 10, verify`() {
        StepVerifier.withVirtualTime { service.getOrdersForCoffee(coffee2.id!!).take(10) }
            .thenAwait(Duration.ofHours(1))
            .expectNextCount(10)
            .verifyComplete()
    }
}
