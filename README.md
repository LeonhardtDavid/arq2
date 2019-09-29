# arq2

Repositorio usado para la materia Arquitectura de Software II, de la Universidad Nacional de Quilmes ([UNQ](http://www.unq.edu.ar)).

## Para ejecutar el proyecto

_Es necesario tener instalado [sbt](https://www.scala-sbt.org/download.html)_

```sh
sbt "~reStart"
```

## Request para pruebas

Hay una colección de Postman con ejemplos para realizar pruebas:  
https://www.getpostman.com/collections/e00af3c985c682a4ba69

## Info del proyecto

El proyecto deriva del hecho para Arquitectura de Software I (https://github.com/arq1nnySu/enjoy-events-back), hecho originalmente en [Python](https://www.python.org), [flask-restplus](https://flask-restplus.readthedocs.io/en/stable/) y [MongoDB](https://www.mongodb.com).

En este caso, el proyecto sigue siendo una aplicación que expone una API Rest Json, pero se migró el stack de tecnologías. Está hecho en [Scala](https://www.scala-lang.org) con [akka-http](https://doc.akka.io/docs/akka-http/current/index.html), [Slick](https://slick.lightbend.com) y [PostgreSQL](https://www.postgresql.org) para base de datos.

La API permite crear y autenticar un usuario, crear, modificar, eliminar y participar de eventos.

| Método | Ruta                        | Descripción                                     | Requiere autenticación |
| ------ | --------------------------- | ----------------------------------------------- | :--------------------: |
| POST   | /api/users                  | Creación de usuarios.                           | Si                     |
| POST   | /api/users/tokens           | Autenticación de usuarios.                      | No                     |
| GET    | /api/users/:id/assistances  | Listar los eventos a los que asiste un usuario. | Si                     |
| POST   | /api/events                 | Creación de eventos.                            | Si                     |
| GET    | /api/events                 | Listar eventos.                                 | Si                     |
| GET    | /api/events/:id             | Obtener un evento.                              | Si                     |
| PUT    | /api/events/:id             | Actualizar un evento.                           | Si                     |
| DELETE | /api/events/:id             | Eliminar un evento.                             | Si                     |
| GET    | /api/events/:id/weather     | Obtener el clima para un evento. Solo si el evento se realiza en menos de 16 días. | Si |
| GET    | /api/events/:id/assistances | Listar quienes asisten a un evento.             | Si                     |
| POST   | /api/events/:id/assistances | Asistir a un evento.                            | Si                     |

Para las rutas que requieren autenticación, es necesario enviar el token generado con `/api/users/tokens` en el header `Authorization` con el contenido: `Bearer {{token}}`.
