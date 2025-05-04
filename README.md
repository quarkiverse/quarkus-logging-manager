<div align="center">

<img src="https://github.com/quarkiverse/.github/blob/main/assets/images/quarkus.svg" width="67" height="70" ><img src="https://github.com/quarkiverse/.github/blob/main/assets/images/plus-sign.svg" height="70" ><img src="https://github.com/quarkiverse/quarkus-logging-manager/blob/main/docs/modules/ROOT/assets/images/logmanager.svg" height="70" >

# Logging Manager
</div>
<br>

[![Maven Central](https://img.shields.io/maven-central/v/io.quarkiverse.loggingmanager/quarkus-logging-manager?color=cool-green&style=flat-square)](https://mvnrepository.com/artifact/io.quarkiverse.loggingmanager/quarkus-logging-manager)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg?style=flat-square)](https://opensource.org/licenses/Apache-2.0)
[![Build](https://github.com/quarkiverse/quarkus-logging-manager/actions/workflows/build.yml/badge.svg)](https://github.com/quarkiverse/quarkus-logging-manager/actions/workflows/build.yml)

The **Quarkus Logging Manager** Extension provides you endpoints to visualize and manage the
log level of your loggers.

| Endpoint                                     | Http Method |                                             Description                                              |
|----------------------------------------------|:-----------:|:----------------------------------------------------------------------------------------------------:|
| `/q/logging-manager`                         |    `GET`    |      Returns the list of all loggers, with information about the configured and effective level      |
| `/q/logging-manager?loggerName={loggerName}` |    `GET`    | Returns the logger specified by this name, with information about the configured and effective level |
| `/q/logging-manager`                         |   `POST`    |                            Changes the log level of the specified logger                             |
| `/q/logging-manager/levels`                  |    `GET`    |                                     Get all the available level                                      |

## Security
Security of endpoints is important, and we do not want to allow unknown people to know (or worse, change!) the log levels of
our applications.
Fortunately we can secure our endpoints using Quarkus' default security mechanism, as described in [Security Overview](https://quarkus.io/guides/security-overview).
All you have to do is define your application.properties similar to this: 

```properties
quarkus.http.auth.basic=true # If you want basic auth. Multiple auth mechanism are supported

quarkus.http.auth.policy.admin-access.roles-allowed=admin
quarkus.http.auth.permission.roles1.paths=/q/logging-manager
quarkus.http.auth.permission.roles1.policy=admin-access
```
And, in case you chose Basic Auth, provide a IdentityProvider (either by implementing one or adding an extension that provides
one).
Quarkus will take care of matching the paths (in this case `/q/logging-manager` to the policy you defined and 
granting or denying access).
Then you can also secure all the endpoints in your application using this configuration.


## Example:

To use this in your application, simply add this in your pom.xml:

```xml
    <dependency>
        <groupId>io.quarkiverse.loggingmanager</groupId>
        <artifactId>quarkus-logging-manager</artifactId>
        <version>${logger-manager.version}</version>
        <scope>runtime</scope>
    </dependency>
```

Note: Replace `${logger-manager.version}` with the latest version

## OpenAPI

You can include the Logger Manager API in the OpenAPI document (and thus also Swagger UI). This needs to be
enabled via config:

```
quarkus.logging-manager.openapi.included=true
```

This will then add the following to your OpenAPI:

![swagger_manager screenshot](openapi.png "Swagger UI Screenshot")

## Roadmap
- [x] OpenApiSpec for the endpoints
- [x] Make endpoint configurable
- [x] Enable customizable security on the endpoint (see readme file)

## Contributors ✨

Thanks goes to these wonderful people ([emoji key](https://allcontributors.org/docs/en/emoji-key)):

<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->
<table>
  <tbody>
    <tr>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/oscarfh"><img src="https://avatars3.githubusercontent.com/u/3311764?v=4?s=100" width="100px;" alt="oscarfh"/><br /><sub><b>oscarfh</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-logging-manager/commits?author=oscarfh" title="Code">💻</a> <a href="#maintenance-oscarfh" title="Maintenance">🚧</a></td>
      <td align="center" valign="top" width="14.28%"><a href="http://www.phillip-kruger.com"><img src="https://avatars3.githubusercontent.com/u/6836179?v=4?s=100" width="100px;" alt="Phillip Krüger"/><br /><sub><b>Phillip Krüger</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-logging-manager/commits?author=phillip-kruger" title="Code">💻</a> <a href="#maintenance-phillip-kruger" title="Maintenance">🚧</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/manofthepeace"><img src="https://avatars.githubusercontent.com/u/13215031?v=4?s=100" width="100px;" alt="manofthepeace"/><br /><sub><b>manofthepeace</b></sub></a><br /><a href="#maintenance-manofthepeace" title="Maintenance">🚧</a> <a href="https://github.com/quarkiverse/quarkus-logging-manager/commits?author=manofthepeace" title="Code">💻</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/spieps"><img src="https://avatars.githubusercontent.com/u/103952931?v=4?s=100" width="100px;" alt="Seth Piepergerdes"/><br /><sub><b>Seth Piepergerdes</b></sub></a><br /><a href="#maintenance-spieps" title="Maintenance">🚧</a> <a href="https://github.com/quarkiverse/quarkus-logging-manager/commits?author=spieps" title="Code">💻</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/ChMThiel"><img src="https://avatars.githubusercontent.com/u/70508469?v=4?s=100" width="100px;" alt="Christian Thiel"/><br /><sub><b>Christian Thiel</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-logging-manager/commits?author=ChMThiel" title="Code">💻</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/survivant"><img src="https://avatars.githubusercontent.com/u/191879?v=4?s=100" width="100px;" alt="Sebastien Dionne"/><br /><sub><b>Sebastien Dionne</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-logging-manager/commits?author=survivant" title="Documentation">📖</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/Koekebakkert"><img src="https://avatars.githubusercontent.com/u/33450925?v=4?s=100" width="100px;" alt="Koekebakkert"/><br /><sub><b>Koekebakkert</b></sub></a><br /><a href="#maintenance-Koekebakkert" title="Maintenance">🚧</a></td>
    </tr>
    <tr>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/stuartwdouglas"><img src="https://avatars.githubusercontent.com/u/328571?v=4?s=100" width="100px;" alt="Stuart Douglas"/><br /><sub><b>Stuart Douglas</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-logging-manager/commits?author=stuartwdouglas" title="Code">💻</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/PierreBtz"><img src="https://avatars.githubusercontent.com/u/9881659?v=4?s=100" width="100px;" alt="Pierre Beitz"/><br /><sub><b>Pierre Beitz</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-logging-manager/commits?author=PierreBtz" title="Documentation">📖</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://melloware.com"><img src="https://avatars.githubusercontent.com/u/4399574?v=4?s=100" width="100px;" alt="Melloware"/><br /><sub><b>Melloware</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-logging-manager/commits?author=melloware" title="Code">💻</a> <a href="#maintenance-melloware" title="Maintenance">🚧</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://lesincroyableslivres.fr/"><img src="https://avatars.githubusercontent.com/u/1279749?v=4?s=100" width="100px;" alt="Guillaume Smet"/><br /><sub><b>Guillaume Smet</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-logging-manager/commits?author=gsmet" title="Code">💻</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/EricWittmann"><img src="https://avatars.githubusercontent.com/u/1890703?v=4?s=100" width="100px;" alt="Eric Wittmann"/><br /><sub><b>Eric Wittmann</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-logging-manager/commits?author=EricWittmann" title="Tests">⚠️</a></td>
      <td align="center" valign="top" width="14.28%"><a href="http://www.xlate.io/"><img src="https://avatars.githubusercontent.com/u/20868526?v=4?s=100" width="100px;" alt="Michael Edgar"/><br /><sub><b>Michael Edgar</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-logging-manager/pulls?q=is%3Apr+reviewed-by%3AMikeEdgar" title="Reviewed Pull Requests">👀</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/alex-kovalenko1982"><img src="https://avatars.githubusercontent.com/u/69167029?v=4?s=100" width="100px;" alt="Alex Kovalenko"/><br /><sub><b>Alex Kovalenko</b></sub></a><br /><a href="#ideas-alex-kovalenko1982" title="Ideas, Planning, & Feedback">🤔</a></td>
    </tr>
    <tr>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/tqvarnst"><img src="https://avatars.githubusercontent.com/u/1204115?v=4?s=100" width="100px;" alt="Thomas Qvarnström"/><br /><sub><b>Thomas Qvarnström</b></sub></a><br /><a href="#ideas-tqvarnst" title="Ideas, Planning, & Feedback">🤔</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/Ladicek"><img src="https://avatars.githubusercontent.com/u/480590?v=4?s=100" width="100px;" alt="Ladislav Thon"/><br /><sub><b>Ladislav Thon</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-logging-manager/commits?author=Ladicek" title="Code">💻</a></td>
    </tr>
  </tbody>
</table>

<!-- markdownlint-restore -->
<!-- prettier-ignore-end -->

<!-- ALL-CONTRIBUTORS-LIST:END -->

This project follows the [all-contributors](https://github.com/all-contributors/all-contributors) specification. Contributions of any kind welcome!

[1]: https://quarkus.io/guides/security-authorization