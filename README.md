# OpticMap

OpticMap is a on-device optical character recognition Android application. Internet connection only needed for downloading models when a language selected first time. It can can recognize text in any Latin-based character set and can translate recognized texts. It supports Catalan, Danish, Dutch, English, Finnish, French, German, Hungarian, Italian, Norwegian, Polish, Portugese, Romanian, Spanish, Swedish, Tagalog, Turkish;

  - Recognize text on images and translate recognized texts.
  - Images can be coming from gallery or camera
  - Save image with recognized text on it
  - Saved images carries additional informations such as location, name of the place, address, date
  - List all saved images


### Tech

* MVVM architecture
* Single activity multiple fragments
* MlKit Translate for translation
* MlKit Text Recognition for OCR
* Coroutine for async tasks
* LiveData
* Timber for logging
* uCrop for cropping images
* CameraView (otaliastudios) for camera preview and taking picturs
* Room for local database
* ViewModel
* SharedPreference

Screenshots:
<p align="center">
  <img src="https://github.com/etasdemir/OpticMap/blob/master/screens/Screenshot_20210619-123903_OpticMap.jpg?raw=true" width="200">
  <img src="https://github.com/etasdemir/OpticMap/blob/master/screens/Screenshot_20210619-123939_OpticMap.jpg?raw=true" width="200">
  <img src="https://raw.githubusercontent.com/etasdemir/OpticMap/master/screens/Screenshot_20210619-124004_OpticMap.jpg?raw=true" width="200">
</p>
<br>
<p align="center">
  <img src="https://github.com/etasdemir/OpticMap/blob/master/screens/Screenshot_20210619-125538_OpticMap.jpg?raw=true" width="200">
  <img src="https://github.com/etasdemir/OpticMap/blob/master/screens/Screenshot_20210619-125944_OpticMap.jpg?raw=true" width="200">
  <img src="https://github.com/etasdemir/OpticMap/blob/master/screens/Screenshot_20210619-125951_OpticMap.jpg?raw=true" width="200">
</p>

License
----

OpticMap is licensed under the Apache License, Version 2.0. See LICENSE for the full license text.

