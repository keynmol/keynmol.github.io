```scala mdoc
println("hello, world!")
import cats.effect._

val io = IO(println("hello")) *> IO(println("world"))

io.unsafeRunSync()
```
