# PIR – Android App

<!-- PROJECT LOGO -->
<br />
<p align="center">
  <a href="https://github.com/PIR-IDS/IDS-Android-App">
    <img src="https://avatars.githubusercontent.com/u/99486891" alt="Logo" width="130">
  </a>

  <p align="center">
    IDS: Code for the Android App
    <br />
    <a href="https://github.com/PIR-IDS/IDS-Android-App/releases"><strong>See Releases »</strong></a>
    <br />
    <br />
    <a href="https://github.com/PIR-IDS/research-paper">Research Paper</a>
    ·
    <a href="https://github.com/PIR-IDS/IDS-Android-App/actions/workflows/test.yml">Test Results</a>
    ·
    <a href="https://github.com/PIR-IDS/.github/blob/main/profile/README.md#usage">See Global Usage</a>
  </p>

<!-- TABLE OF CONTENTS -->
<details open="open">
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
      <ul>
        <li><a href="#built-with">Built With</a></li>
      </ul>
    </li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#prerequisites">Prerequisites</a></li>
        <li><a href="#installation">Installation</a></li>
      </ul>
    </li>
    <li>
      <a href="#usage">Usage</a>
      <ul>
        <li><a href="#execution">Execution</a></li>
        <li><a href="#tests">Tests</a></li>
        <li><a href="#generation">Generation</a></li>
        <ul>
           <li><a href="#documentation">Documentation</a></li>
        </ul>
      </ul>
    <li><a href="#contribute">Contribute</a></li>
    <li><a href="#tree-structure">Tree Structure</a></li>
    <li><a href="#credits">Credits</a></li>
    <li><a href="#contact">Contact</a></li>

  </ol>
</details>

***

<!-- ABOUT THE PROJECT -->
## About The Project

This code will be used in order to receive the anomalies detected by the Arduino and to check if an event is linked to a distant action.

<p align="center">
    <img src="https://user-images.githubusercontent.com/26198903/183397746-a4b756ff-0a04-4860-b5f5-d9b670533ae8.png" alt="Logo" width="24%">
    <img src="https://user-images.githubusercontent.com/26198903/183397792-bd406f19-d944-4a71-82bd-4927cf24eb69.png" alt="Logo" width="24%">
    <img src="https://user-images.githubusercontent.com/26198903/183397818-069b1ccc-ddb3-4f8e-b75d-0654b0a27d48.png" alt="Logo" width="24%">
    <img src="https://user-images.githubusercontent.com/26198903/183397891-2ae35514-fbb9-48b9-b5f0-8e27e3cda6c1.png" alt="Logo" width="24%">
</p>

### Built With
* [Kotlin](https://kotlinlang.org/)
* [Android](https://developer.android.com/)
* [Jetpack Compose](https://developer.android.com/jetpack/compose/)
* [Jetpack Room](https://developer.android.com/jetpack/androidx/releases/room/)

<!-- GETTING STARTED -->
## Getting Started

### Prerequisites

* [Android Studio](https://developer.android.com/studio) is the preferred way to use this project.

### Installation

1. Clone the project
   ```sh
   git clone https://github.com/PIR-IDS/IDS-Android-App.git
   ```
2. IDS Android App is now ready to run.

<!-- USAGE EXAMPLES -->
## Usage

### Execution

_TODO_

### Tests

_TODO_

### Generation

_TODO_

#### Documentation

_TODO_

<!-- CONTRIBUTE -->
## Contribute

You will find in this section how to add new services and devices to the project.

<details>
  <summary>Add a Service to the compatibility list</summary>

To add a new service to the compatible ones, you need to add the necessary resources and then edit some files.

### A. Resources

1. Add an square icon for the service in the folder `app/src/main/res/drawable-nodpi` in PNG, for example `my_service_logo.png`.
2. Add a string resource for the service description in the folder `app/src/main/res/values/strings.xml`, for example `my_service_description`. Do not forget to translate the string in all the languages supported by the app.

### B. Sources

1. Add an enumeration to ServiceId with a unique tag in `app/src/main/java/fr/pirids/idsapp/data/items/Service.kt`. Also add the service to the Service list with the associated devices that will be used to detect the behavioural anomalies.
2. Add a way to handle the service credentials necessary to interact with the API in a new created file in `app/src/main/java/fr/pirids/idsapp/data/api/auth`, named `MyServiceAuth.kt`. This class has to inherit from `ApiAuth`. You will have to provide a way to instanciate this class each time a connection has to be made, notably during the `when` statements in each of these files: `app/src/main/java/fr/pirids/idsapp/controller/detection/Service.kt`.
3. Add a way to handle the service data you get from the API in a new created file in `app/src/main/java/fr/pirids/idsapp/data/api/data`, named `MyServiceData.kt`. This class has to inherit from `ApiData`. You will have to provide a way to instanciate this class each time data has to be retrieved, notably during the `when` statements in each of these files: `app/src/main/java/fr/pirids/idsapp/controller/detection/Service.kt`, `app/src/main/java/fr/pirids/idsapp/ui/views/service/ServiceView.kt`, `app/src/main/java/fr/pirids/idsapp/controller/detection/Detection.kt`, `app/src/main/java/fr/pirids/idsapp/controller/daemon/ServiceDaemon.kt`.
4. Create a class that will handle the connection to the service, which inherits from `ApiInterface` in a new created file in `app/src/main/java/fr/pirids/idsapp/controller/api`, named `MyServiceApi.kt`. You will have to provide a way to instanciate this class each time a connection has to be made, notably during the `when` statements in each of these files: `app/src/main/java/fr/pirids/idsapp/controller/detection/Service`, `app/src/main/java/fr/pirids/idsapp/controller/daemon/ServiceDaemon.kt`.
5. Add the persistence of the credentials and the data retrieved by creating the entity and DAO linked to the new service. Create a new file in `app/src/main/java/fr/pirids/idsapp/data/model/entity/service` named `MyServiceAuth.kt`. Link a foreign key to the `ApiAuth` entity id. Create a new file in `app/src/main/java/fr/pirids/idsapp/data/model/entity/service` named `MyServiceData.kt`. Link a foreign key to the `ApiData` entity id. Register the newly created entities into the `app/src/main/java/fr/pirids/idsapp/data/model/AppDatabase.kt` file. Now create the DAO for the new service, following the same logic in the `app/src/main/java/fr/pirids/idsapp/data/model/dao` package. Call them `MyServiceAuthDao` and `MyServiceDataDao` and add their implementation to the `app/src/main/java/fr/pirids/idsapp/data/model/AppDatabase.kt` file. You will have to use the DAO notably during the `when` statements in each of these files: `app/src/main/java/fr/pirids/idsapp/controller/detection/Service`, `app/src/main/java/fr/pirids/idsapp/controller/daemon/ServiceDaemon.kt`, `app/src/main/java/fr/pirids/idsapp/controller/detection/Detection.kt`.

</details>

<details>
  <summary>Add a BLE Device support</summary>

To support a new BLE device, you need to add the necessary resources and then edit some files.

### A. Resources

1. Add an square icon for the device in the folder `app/src/main/res/drawable-nodpi` in PNG, for example `ids_device_name_logo.png`.
2. Add a string resource for the device description in the folder `app/src/main/res/values/strings.xml`, for example `device_name_desc`. Also add a data name, for example `device_name_data`, an event message, for example `device_name_event_message` and an intrusion message, for example `device_name_intrusion`. Do not forget to translate all the strings in all the languages supported by the app.

### B. Sources

1. Add an enumeration to DeviceId with a unique tag in `app/src/main/java/fr/pirids/idsapp/data/items/Device.kt`. Also add the device to the Device list with the associated Bluetooth services that will be used to transmit the data. You can add the services with their characteristics in their respective files if they are still not added.
3. Add a way to handle the service data you get from the device in a new created file in `app/src/main/java/fr/pirids/idsapp/data/device/data`, named `MyDeviceData.kt`. This class has to inherit from `DeviceData`, you can also add some Bluetooth characteristics you would want to store during runtime in there. You will have to provide a way to instanciate this class each time data has to be used, notably during the `when` statements in each of these files: `app/src/main/java/fr/pirids/idsapp/controller/view/menus/NotificationViewController.kt`, `app/src/main/java/fr/pirids/idsapp/ui/views/service/DeviceView.kt`, `app/src/main/java/fr/pirids/idsapp/controller/detection/Detection.kt`, `app/src/main/java/fr/pirids/idsapp/controller/daemon/DeviceDaemon.kt`, `app/src/main/java/fr/pirids/idsapp/controller/bluetooth/Device.kt`, `app/src/main/java/fr/pirids/idsapp/controller/bluetooth/BluetoothConnection.kt`. You will have to handle the BLE communication in `app/src/main/java/fr/pirids/idsapp/controller/bluetooth/BluetoothConnection.kt`.
5. Add the persistence of the device data retrieved by creating the entity and DAO linked to the new device. Create a new file in `app/src/main/java/fr/pirids/idsapp/data/model/entity/device` named `MyDeviceData.kt`. Link a foreign key to the `DeviceData` entity id. Register the newly created entity into the `app/src/main/java/fr/pirids/idsapp/data/model/AppDatabase.kt` file. Now create the DAO for the new device, following the same logic in the `app/src/main/java/fr/pirids/idsapp/data/model/dao` package. Call it `MyDeviceDataDao` and add its implementation to the `app/src/main/java/fr/pirids/idsapp/data/model/AppDatabase.kt` file. You will have to use the DAO notably during the `when` statements in each of these files: `app/src/main/java/fr/pirids/idsapp/controller/daemon/DeviceDaemon.kt`, `app/src/main/java/fr/pirids/idsapp/controller/bluetooth/BluetoothConnection.kt`.

</details>

***

<!-- TREE STRUCTURE -->
## Tree Structure
<details>

_TODO_

</details>

<!-- CREDITS -->
## Credits

Romain Monier [ [GitHub](https://github.com/rmonier) ] – Co-developer
<br>
Morgan Pelloux [ [GitHub](https://github.com/MonsieurSinge) ] – Co-developer
<br>
David Violes [ [GitHub](https://github.com/ViolesD) ] – Co-developer
<br>
Amélie Muller [ [GitHub](https://github.com/AmelieMuller) ] – Co-developer
<br>
Malik Sedira [ [GitHub](https://github.com/sediramalik) ] – Co-developer
<br>
Quentin Douarre [ [GitHub](https://github.com/Quintus618) ] – Co-developer
<br>
Noé Chauveau [ [GitHub](https://github.com/Noecv) ] – Co-developer
<br>
Pierre Favary [ [GitHub](https://github.com/pdf-0) ] – Co-developer

<!-- CONTACT -->
## Contact

Project Link : [https://github.com/PIR-IDS/IDS-Android-App](https://github.com/PIR-IDS/IDS-Android-App)

Organization Link : [https://github.com/PIR-IDS](https://github.com/PIR-IDS)
