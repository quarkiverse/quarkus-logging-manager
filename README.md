# Logging UI Extension 
[![Maven Central](https://img.shields.io/maven-central/v/io.quarkiverse.loggingui/quarkus-logging-ui?color=cool-green&style=flat-square)](https://mvnrepository.com/artifact/io.quarkiverse.loggingui/quarkus-logging-ui)
[![All Contributors](https://img.shields.io/badge/all_contributors-1-orange.svg?color=cool-green&style=flat-square)](#contributors-)

This is the alpha version of the Quarkus Logging UI Extension, it provides you endpoints to visualize and manage the
log level of your loggers.
Currently, there is no authentication/authorization mechanisms in place to protect from unauthorized access, in this 
alpha version you have to protect this endpoint by yourself. 

| Endpoint        | Http Method           | Description  |
| ------------- |:-------------:|:-----:|
| `/loggers`      | `GET` | Returns the list of all loggers, with information about the configured and effective level |
| `/loggers/{loggerName}`     | `GET`      |   Returns the logger specified by this name, with information about the configured and effective level |
| `/loggers/{loggerName}` | `POST`      |    Changes the log level of the specified logger |

## Example:
> TODO

## Roadmap
 * Make endpoint configurable
 * Enable customizable security on the endpoint
 * Graphical UI to read logger level 
 * OpenApiSpec for the endpoints

## Contributors ✨

Thanks goes to these wonderful people ([emoji key](https://allcontributors.org/docs/en/emoji-key)):

<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->
<table>
  <tr>
    <td align="center"><a href="https://github.com/oscarfh"><img src="https://avatars3.githubusercontent.com/u/3311764?v=4" width="100px;" alt=""/><br /><sub><b>oscarfh</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkiverse-logger-ui/commits?author=oscarfh" title="Code">💻</a> <a href="#maintenance-oscarfh" title="Maintenance">🚧</a></td>
  </tr>
</table>

<!-- markdownlint-enable -->
<!-- prettier-ignore-end -->
<!-- ALL-CONTRIBUTORS-LIST:END -->

This project follows the [all-contributors](https://github.com/all-contributors/all-contributors) specification. Contributions of any kind welcome!