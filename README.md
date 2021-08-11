# Lexis — an immersive language learning app

## Table of Contents
1. [Demo](#demo-header)
1. [Overview](#overview-header)
1. [Product Spec](#product-spec-header)
1. [Wireframes](#wireframes-header)
2. [Schema](#schema-header)

<a name="demo-header"></a>
## Demo
Watch my demo video for a quick 2-minute intro of what Lexis is all about, or see below for walkthrough GIFs of individual features!

https://user-images.githubusercontent.com/17547686/129096818-e587ccce-e38d-4ba8-8366-9f46ee932222.mov

### Feature Walkthroughs

| Home feed | Vocabulary |
| -------- | -------- |
| ![home-feed](https://user-images.githubusercontent.com/17547686/129098735-139fbb1d-a738-4155-ac48-9a67133e0856.gif)| ![vocabulary](https://user-images.githubusercontent.com/17547686/129100653-1ac4dae8-2a11-49d9-aa1f-af58abc4577b.gif) |

| Flashcards | Word search puzzle |  
| -------- | -------- | 
| ![flashcards](https://user-images.githubusercontent.com/17547686/129101019-9e14df00-47d1-4358-a512-e7f35324a8a5.gif) | ![word-search](https://user-images.githubusercontent.com/17547686/129101515-2766a368-1360-4ea1-b016-df783f745224.gif) |

| Profile + settings | Login & signup |
| -------- | -------- |
| ![profile](https://user-images.githubusercontent.com/17547686/129101755-8d375370-baf7-4dac-b214-1482f0f1b2e7.gif) | ![login](https://user-images.githubusercontent.com/17547686/129102657-442f2572-0797-45a6-b9a6-a9b814ac286c.gif) |

<a name="overview-header"></a>
## Overview
### Description
It can be difficult to find the motivation or time to learn a new language — so what if you could learn new words while reading regular internet content? Lexis is a language learning app designed to be immersive and fun. Users can see translated words embedded within internet content for in-context learning and practice in a fun way through flashcards & word search puzzles. 

### App Evaluation
- **Category:** Education
- **Mobile:** Allows language learning on the go + games and quizzes are very suited for mobile 
- **Story:** Users can simultaneously learn their target language and get more information / current news on topics that they're interested in. Games allow them to practice their vocabulary in a more "traditional" language learning context. 
- **Market:** Anybody who wants to practice their target language could potentially use this app. One challenge would be to ensure the difficulty is appropriate for the level of the learner (but this is probably beyond the scope of my FBU project).
- **Habit:** Could display stats + push notifications about their vocabulary learning to keep users coming back. The content being curated to the user's interests would also help them use the app more. 
- **Scope:** V1 would fetch content from an outside source and translate random words within it into the target language, plus show the translations + meaning of the words. V2 would incorporate a vocabulary view where users can see words that they've previously seen, and games / flashcards for additional practice. V3 would allow users to choose the type of content that they want to see. V4 would allow users to upload their own content. 

<a name="product-spec-header"></a>
## Product Spec
### 1. User Stories (Required and Optional)

**Required Must-have Stories**

* User can see content with random translated words 
* User can see a feed with content
* User can see the meaning of translated words 
* User can select and update their target language 
* User can see a list of vocabulary that they've encountered 
* User can sign up for a new account 
* User can log in 

**Optional Nice-to-have Stories**

* User can upload their own content to have translated
* User can save content to re-visit later 
* User can star / otherwise mark difficult words for extra practice
* User can receive push notifications to encourage them to come back to the app
* User can take notes within the app about content / vocabulary
* User can see curated content by category / topic 
* User can add friends and see statistics about their friends' language learning
* User can share content from within the app with friends
* User can practice their vocabulary through games or flashcards
* User can see statistics about their language learning (# of words learned, time spent, etc.) 
* User can see example sentences in target language + English for translated words

### 2. Screen Archetypes

* Login screen
    * User can log in
* Registration screen
    * User can sign up for a new account
* Feed
    * User can see content with random translated words
    * User can see the meaning of translated words 
* Vocabulary
    * User can see a list of vocabulary that they've encountered
* Games
    * User can practice their vocabulary through games or flashcards
* Profile
    * User can see statistics about their language learning (# of words learned, time spent...)
* Settings
    * User can select and update their target language

### 3. Navigation

**Tab Navigation** (Tab to Screen)

* Home feed
* Practice
* Profile

**Flow Navigation** (Screen to Screen)

* Home feed
    => Games
* Registration screen / log in screen
    => Home feed
* Games
    => Flashcards
    => Writing quiz
* Profile
    => Settings
    => Vocabulary

<a name="wireframes-header"></a>
## Wireframes
[Link to Figma wireframes](https://www.figma.com/file/iQN1JEZPj5flUwn1eo0e6l/FBU-App-Wireframes?node-id=0%3A1)
### Home Tab
![](https://i.imgur.com/1YrDqLO.png)
### Practice Tab
![](https://i.imgur.com/ssMwHSJ.png)
### Profile Tab
![](https://i.imgur.com/UKVVTAp.png)

<a name="schema-header"></a>
## Schema 
### Models
**Word**
| Property | Type | Description |
| -------- | -------- | -------- |
| objectId | String | unique id for each word (default field) |
| english | String | English meaning of word |
| target | String | translation of word in target language |
| isStarred | Boolean | whether the word is starred |
| createdAt	| DateTime | date when word is created (default field) |
| updatedAt	| DateTime | date when word is last updated (default field)|

**Article**
| Property | Type | Description |
| -------- | -------- | -------- |
| objectId | String | unique id for each article (default field) |
| source | String | API where the content is fetched from (e.g. New York Times) |
| indices | Array | array of indices of words that are translated |
| title | String | title of the article |
| body | String | body text of the article |
| difficulty | Number | difficulty level of the article (?) |
| isSaved | Boolean | whether the article is saved |
| createdAt	| DateTime | date when article is created (default field) |
| updatedAt	| DateTime | date when article is last updated (default field)|

**User**
| Property | Type | Description |
| -------- | -------- | -------- |
| objectId | String | unique id for each user (default field) |
| username | String | username (default field) |
| password | String | password (default field) |
| targetLanguage | String | language code for user's target language |

### Networking
#### List of network requests by screen
* Sign up screen
    * (Create/POST) Create new User object
* Log in screen
    * (Read/GET) Query User object
* Home (feed) screen
    * (Read/GET) Query all articles
* Article screen
    * (Read/GET) Query article with given objectId (or will it already be stored locally?)
    * (Create/POST) Add word to Parse database when tapped
    * (Read/GET) Query all words that belong to this article (will be storing locally until tapped?)
    * (Update/PUT) Update whether a word is starred
    * (Update/PUT) Update whether an article is saved
* Practice screen
    * (Read/GET) Query all words (or only starred words)
    * (Update/PUT) Update whether a word is starred
* Profile screen
    * (Read/GET) Query information about current user
    * (Update/PUT) Update user target language
* Vocabulary screen
    * (Read/GET) Query all words (or only starred words)
    * (Update/PUT) Update whether a word is starred

### Existing API endpoints
**Google Cloud Translation API**
* Base URL - https://translation.googleapis.com/language/translate/v2
* Docs - https://cloud.google.com/translate/docs/reference/rest/v2/translate

| HTTP Verb | Endpoint | Description | Parameters |
| -------- | -------- | -------- | -------- | 
| POST     | /   | translate input text  | q - input text (string) <br /> target - target language (string) <br /> source - source language (string) <br /> key - API key (string) |
| POST     | /detect   | detect language of input text  | q - input text (string) <br /> key - API key (string) |
| GET     | /languages   | return a list of languages supported for translation   | key - API key (string) |

**New York Times APIs**
* Docs - https://developer.nytimes.com/apis

#### Most Popular API
* Base URL - https://api.nytimes.com/svc/mostpopular/v2

| HTTP Verb | Endpoint | Description | Parameters |
| -------- | -------- | -------- | -------- | 
| GET     | /viewed/{period}.json   | returns an array of the most viewed articles on NYTimes.com for specified period of time (1 day, 7 days, or 30 days) | api-key - API key (string) |

#### Top Stories API
* Base URL - https://api.nytimes.com/svc/topstories/v2
* The possible section value are: arts, automobiles, books, business, fashion, food, health, home, insider, magazine, movies, nyregion, obituaries, opinion, politics, realestate, science, sports, sundayreview, technology, theater, t-magazine, travel, upshot, us, and world.

| HTTP Verb | Endpoint | Description | Parameters |
| -------- | -------- | -------- | -------- | 
| GET     | /{section}.json   | returns an array of articles currently on the specified section | api-key - API key (string) |

**Wikipedia API**
* Base URL - https://wikimedia.org/api/rest_v1/
* Docs - https://wikimedia.org/api/rest_v1/#/

| HTTP Verb | Endpoint | Description | Parameters |
| -------- | -------- | -------- | -------- | 
| GET     | /metrics/pageviews/top/en.wikipedia.org/all-access/{year}/{month}/{day}   | returns the most viewed articles on Wikipedia | year - year in YYYY format (string) <br /> month - month in MM format (string) <br /> day - day in DD format, "all-days" if fetching top articles for whole month (string) |

**Collins API**
* Docs - http://www.collinslanguage.com/collins-api/
* For example sentences
