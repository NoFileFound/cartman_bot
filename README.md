# Cartman (https://scammer.info/u/cartman/summary)
Cartman is both discord application and scammer.info account with purpose of reporting Phishing websites and phone numbers. It supports lookup command for ip address, domain or phone number. The discord bot also supports person verification in order to use some of the commands (lookup and reporting).

# Building the project
You need to have Intellij IDEA (I use 2024.1) and the following api keys:
+ https://api.ipdata.co/ (IP information)
+ https://apilayer.net (Phone number information)
+ https://urlscan.io/ (Scan urls)
+ https://playit.gg/ (Report endpoints)
+ Google safebrowsing (lookup and reporting)
You also will need to have custom SMTP. (For sending emails).

# Discord commands
- /whois [domain|ip|mobile] [query] - Fetches information about the specific category (Whois for domain, IPData for specific ip address and apilayer for the mobile number).
- /report [url] [query] [reason] - Reports the url to google safebrowsing, urlscan and maybe creating a topic here (but I removed the code due to abuse in the system). The reasons are phishing, TSS and malware. (If url is from playitt, it will automatically use their api for abuse). If the reason is TSS it will use the same scheme how i make topics here. It uses proxy to go that website, take screenshot and get the content of it. Phone numbers in US, JP are only supported. (Slight support for FR and DE).
- /refreshproxies - Refreshes the list of proxies.
- /shutdown - Shutdowns the bot.
- /cpuinfo - Information about the hosting machine/PC and ping from the JDA.