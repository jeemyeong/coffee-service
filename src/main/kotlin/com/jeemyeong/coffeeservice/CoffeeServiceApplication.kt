package com.jeemyeong.coffeeservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import java.time.Duration
import java.time.Instant
import javax.annotation.PostConstruct

@SpringBootApplication
class CoffeeServiceApplication

fun main(args: Array<String>) {
    runApplication<CoffeeServiceApplication>(*args)
}

@RestController
@RequestMapping("/coffees")
class CoffeeController(private val service: CoffeeService) {
    @GetMapping
    fun all() = service.getAllCoffees()

    @GetMapping("/{id}")
    fun byId(@PathVariable id: String) = service.getCoffeeById(id)

    @GetMapping("/{id}/orders", produces = arrayOf(MediaType.TEXT_EVENT_STREAM_VALUE))
    fun orders(@PathVariable id: String) = service.getOrdersForCoffee(id)
}

@Service
class CoffeeService(private val repo: CoffeeRepo) {
    fun getAllCoffees() = repo.findAll()

    fun getCoffeeById(id: String) = repo.findById(id)

    fun getOrdersForCoffee(coffeeId: String) = Flux.interval(Duration.ofSeconds(1))
        .onBackpressureBuffer()
        .map { CoffeeOrder(coffeeId, Instant.now()) }
}

@Component
class DataLoader(private val repo: CoffeeRepo) {
    @PostConstruct
    private fun load() =
        repo.deleteAll().thenMany(
            Flux.just("latte", "cappucino", "espresso", "macchiato", "doppio")
            .map { Coffee(name = it) }
            .flatMap { repo.save(it) })
                .thenMany(repo.findAll())
            .subscribe { println(it) }

}

interface CoffeeRepo: ReactiveCrudRepository<Coffee, String>

class CoffeeOrder(
    val coffeeId: String,
    val whenOrdered: Instant
)

@Document
data class Coffee(
    @Id val id: String? = null,
    val name: String = "Any old joe"
)
