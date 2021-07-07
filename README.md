# Immersive Language Learning App (Name TBD)

## Overview
### Description
Fetches articles / reading material on desired topics from various sources like the New York Times, Wikipedia, books, or poems, but translates random words into target learning language to allow language learning in context. Users can also study seen words through flashcards / writing quizzes or upload their own content. 

### App Evaluation
- **Category:** Education
- **Mobile:** Allows language learning on the go, plus games and quizzes are very suited for mobile. It could also use the camera for uploading content (?). 
- **Story:** Users can simultaneously learn their target language and get more information / current news on topics that they're interested in. Games allow them to practice their vocabulary in a more "traditional" language learning context. 
- **Market:** Anybody who wants to practice their target language could potentially use this app. One challenge would be to ensure the difficulty is appropriate for the level of the learner (but this is probably beyond the scope of my FBU project).
- **Habit:** Could display stats + push notifications about their vocabulary learning to keep users coming back. The content being curated to the user's interests would also help them use the app more. 
- **Scope:** V1 would fetch content from an outside source and translate random words within it into the target language, plus show the translations + meaning of the words. V2 would incorporate a vocabulary view where users can see words that they've previously seen, and games / flashcards for additional practice. V3 would allow users to choose the type of content that they want to see. V4 would allow users to upload their own content. 

## Product Spec

### 1. User Stories (Required and Optional)

**Required Must-have Stories**

* User can see content with random translated words 
* User can see the meaning of translated words 
* User can select and update their target language 
* User can see a list of vocabulary that they've encountered 
* User can practice their vocabulary through games or flashcards
* User can see statistics about their language learning (# of words learned, time spent, etc.) 
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

## Wireframes
[Link to Figma wireframes](https://www.figma.com/file/iQN1JEZPj5flUwn1eo0e6l/FBU-App-Wireframes?node-id=0%3A1)
### Home Tab
![](https://i.imgur.com/1YrDqLO.png)
### Practice Tab
![](https://i.imgur.com/ssMwHSJ.png)
### Profile Tab
![](https://i.imgur.com/UKVVTAp.png)

### [BONUS] Digital Wireframes & Mockups

### [BONUS] Interactive Prototype

## Schema 
[This section will be completed in Unit 9]
### Models
[Add table of models]
### Networking
- [Add list of network requests by screen ]
- [Create basic snippets for each Parse network request]
- [OPTIONAL: List endpoints if using existing API such as Yelp]
