## Установка Docker Compose
_Примечание: Все команды выполняем в терминале Linux_<br>
<br>
Проверяем наличие Docker:
```
docker --version
```
Проверяем наличие Docker Compose:
```
docker-compose --version
```
Если Docker Compose не установлен, то сначала посмотрите какие версии доступны на странице релизов
```
https://github.com/docker/compose/releases
```
Скачиваем пакет:
```
sudo curl -L "https://github.com/docker/compose/releases/download/v2.29.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
```
_Примечание: команда загружает версию v2.29.0 и сохраняет исполняемый файл в каталоге
```/usr/local/bin/docker-compose``` после чего программа будет доступна под именем ```docker-compose```_

Выделяем права:
```
sudo chmod +x /usr/local/bin/docker-compose
```

Если все получилось, то снова проверяем версию.

