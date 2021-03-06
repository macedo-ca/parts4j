# Part4J Java API introduction
This document describes the key concepts when working with parts4j Part and PartType instances.
The parts4j framework standardizes access to common module capabilities and aspects to simplify management of modules.
Key to successfully using the framework is to understand the common variables and actions that can be registered with the framework. These actions and variables represent common capabilities, and parts4j facilities consolidated access and general management features on top of these actions and variables.

## All variables
- [id](#id) - the unique ID of the part
- [health](#health) - the current health of the part (e.g. 'ok', 'crashed')
- [stats](#stats) - statistics for the part (e.g. '500 ms/call', '52344 cache entries')
- [errors](#errors) - significant errors (i.e. errors that has caused or will cause unwanted behaviour) for the part
- [tenant](#tenant) - the tenant identifier (e.g. a client ID or account ID) or details for the part
- [version](#version) - the version of part (such as a module release version)
- [status](#status) - the current status of the part (e.g. life-cycle status such as 'started' or 'stopped')
- [licence](#licence) - the software licence for the part


## All actions
- [create](#create) - creates a new instance
- [install](#install) - performs initial install of the part
- [deploy](#deploy) - deploys the part
- [start](#start) - starts the part
- [stop](#stop) - stops the part
- [suspend](#suspend) - temporarily suspends the part
- [resume](#resume) - resumes a suspended part
- [destroy](#destroy) - destroys (un-recoverable) a part


# Part variables
Parts can register a standard set of fields or variables with the parts4j framework. Variables can either be referenced, by registering the getter method with the framework OR it can be provided programmtically to by calling setter method on the part instance. Either way the current value of the variable can be accessed on the part instance by calling the get[Variable] on the part object.

<a name='id'></a>
## 'id' variable
The variable 'id' is the unique ID of the part and has the following methods:
- registerId(Supplier) - to specify the method that retrieves the id
- setId(String) - to set id
- getId - to retrieve current id

For querying, to find parts by the 'id' field use _queryId(String)_ query method.

In addition, the 'id' variable can be set during the registration of the part.
For example: Assuming as class that has a 'getLocalId' method:
```Java
PartRegistry.registerPart(this,this::getLocalId)
```
 
<a name='health'></a>
## 'health' variable
The variable 'health' is the current health of the part (e.g. 'ok', 'crashed') and has the following methods:
- registerHealth(Supplier) - to specify the method that retrieves the health
- setHealth(String) - to set health
- getHealth - to retrieve current health

To use the 'unhealthy' searches or health-aggregation features of Parts4J, the parts must return valid health indicators. By default the following values are considered healthy:
- 200
- ok
- true
- yes
- null
- good
- green

The list of healthy status codes can be customized through the 'parts4j.healthy' property in parts4j.properties. If a health method or value is not set, the framework will see if there are errors on the part, and report it as unhealthy if there are errors. Unhealthy parts are found with queryUnhealthy() query method.

<a name='stats'></a>
## 'stats' variable
The variable 'stats' is statistics for the part (e.g. '500 ms/call', '52344 cache entries') and has the following methods:
- registerStats(Supplier) - to specify the method that retrieves the stats
- setStats(String) - to set stats
- getStats - to retrieve current stats


<a name='errors'></a>
## 'errors' variable
The variable 'errors' is significant errors (i.e. errors that has caused or will cause unwanted behaviour) for the part and has the following methods:
- registerErrors(Supplier) - to specify the method that retrieves the errors
- setErrors(String) - to set errors
- getErrors - to retrieve current errors

Parts with errors are found with queryHasErrors() query method.
To use the 'unhealthy' searches or health-aggregation features of Parts4J, the parts must return valid health indicators. By default the following values are considered healthy:
- 200
- ok
- true
- yes
- null
- good
- green

The list of healthy status codes can be customized through the 'parts4j.healthy' property in parts4j.properties. If a health method or value is not set, the framework will see if there are errors on the part, and report it as unhealthy if there are errors. Unhealthy parts are found with queryUnhealthy() query method.

<a name='tenant'></a>
## 'tenant' variable
The variable 'tenant' is the tenant identifier (e.g. a client ID or account ID) or details for the part and has the following methods:
- registerTenant(Supplier) - to specify the method that retrieves the tenant
- setTenant(String) - to set tenant
- getTenant - to retrieve current tenant

For querying, to find parts by the 'tenant' field use _queryTenant(String)_ query method.


<a name='version'></a>
## 'version' variable
The variable 'version' is the version of part (such as a module release version) and has the following methods:
- registerVersion(Supplier) - to specify the method that retrieves the version
- setVersion(String) - to set version
- getVersion - to retrieve current version

For querying, to find parts by the 'version' field use _queryVersion(String)_ query method.


<a name='status'></a>
## 'status' variable
The variable 'status' is the current status of the part (e.g. life-cycle status such as 'started' or 'stopped') and has the following methods:
- registerStatus(Supplier) - to specify the method that retrieves the status
- setStatus(String) - to set status
- getStatus - to retrieve current status

For querying, to find parts by the 'status' field use _queryStatus(String)_ query method.


<a name='licence'></a>
## 'licence' variable
The variable 'licence' is the software licence for the part and has the following methods:
- registerLicence(Supplier) - to specify the method that retrieves the licence
- setLicence(String) - to set licence
- getLicence - to retrieve current licence

For querying, to find parts by the 'licence' field use _queryLicence(String)_ query method.



# Part actions

Parts can register a standard set of actions with the parts4j framework. By registering the methods, the part becomes consistently managed with other parts in the environment. These actions have do[action-name] methods on the Part object.

<a name='create'></a>
## 'create' action
The action 'create' creates a new instance and has the following methods:
- registerCreate(Supplier) - to specify the method that creates the part
- registerCreate(Runnable) - to specify void method that creates the part
- doCreate - to create the part

<a name='install'></a>
## 'install' action
The action 'install' performs initial install of the part and has the following methods:
- registerInstall(Supplier) - to specify the method that installs the part
- registerInstall(Runnable) - to specify void method that installs the part
- doInstall - to install the part

<a name='deploy'></a>
## 'deploy' action
The action 'deploy' deploys the part and has the following methods:
- registerDeploy(Supplier) - to specify the method that deploys the part
- registerDeploy(Runnable) - to specify void method that deploys the part
- doDeploy - to deploy the part

<a name='start'></a>
## 'start' action
The action 'start' starts the part and has the following methods:
- registerStart(Supplier) - to specify the method that starts the part
- registerStart(Runnable) - to specify void method that starts the part
- doStart - to start the part

<a name='stop'></a>
## 'stop' action
The action 'stop' stops the part and has the following methods:
- registerStop(Supplier) - to specify the method that stops the part
- registerStop(Runnable) - to specify void method that stops the part
- doStop - to stop the part

<a name='suspend'></a>
## 'suspend' action
The action 'suspend' temporarily suspends the part and has the following methods:
- registerSuspend(Supplier) - to specify the method that suspends the part
- registerSuspend(Runnable) - to specify void method that suspends the part
- doSuspend - to suspend the part

<a name='resume'></a>
## 'resume' action
The action 'resume' resumes a suspended part and has the following methods:
- registerResume(Supplier) - to specify the method that resumes the part
- registerResume(Runnable) - to specify void method that resumes the part
- doResume - to resume the part

<a name='destroy'></a>
## 'destroy' action
The action 'destroy' destroys (un-recoverable) a part and has the following methods:
- registerDestroy(Supplier) - to specify the method that destroys the part
- registerDestroy(Runnable) - to specify void method that destroys the part
- doDestroy - to destroy the part


