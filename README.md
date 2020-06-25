# PhenoManagerApi

API Rest developed using *Spring Boot*, responsible for providing the data managed by the system.
In this project, there is also a Worker service, which processes asynchronous messages regarding executions of scientific workflows, tasks, jobs and other commands that may be setup by the platform. For authentication *OAuth 2.0* and JWT was used.


## Setting up

To test the project locally, simply clone the repository and import into *Eclipse* (or any other IDE of your choice) as a *Gradle* project.
After that, you will need to run the `gradle` build and run the SQLs from the *Docs / SQLs / 1.0.0* folder in your local *PostgreSQL* installation.

## Computational Model configuration

The platform allows the user to setup executors for the following types of computational models:

- WORKFLOW

- EXECUTABLE

- COMMAND

- WEB SERVICE

The platform allows the user to setup execution environments of the following types:

- CLOUD (AWS)

- CLUSTER (LNCC)

- SSH

There is also a possibility to setup VPN conection for these environemnts:

- VPN

- VPNC (Cisco VPN)

## API

The API accepts filters, sorting, aggregation functions, grouping and field projection.
Another important aspect is that the API will only answer *success* if the correct credentials are present in the *header* of the request.
Every request (except for the login endpoint and some other specific endpoints) must contain the `Authorization` header with the value `Bearer {AUTENTICATION_TOKEN}`.
An authentication token can be obtained by making a request to `/login` endpoint twith the correct credentials.
Here is an example of a possible filter operation:

`localhost:9500/v1/computational_models?count=[name,currentVersion]&sort=[creationDate=asc]&groupBy=[creationDate]&filter=[currentVersion>1.0]`

### Filter
The available options of filters to be applied:

- Equals: "=eq=" or "=" (may be used to compare if value is equal to `null`)

- Less than or equal: "=le=" or "<="

- Greater than or equal: "=ge=" or ">="

- Greater than: "=gt=" or ">"

- Less than: "=lt=" or "<"

- Not equal: "=ne=" or "!=" (may be used to compare if the value is other than `null`)

- In: "=in="

- Out: "=out="

- Like: "=like="

Logical operators in the url:

- AND: "\_and\_" or just ";"
- OR: "\_or\_" or just ","

### Projection
The Projection follows the following syntax, and the json response will only have with the specified fields:

`projection = [field1, field2, field3...]`

### Sort
The Sorting follows the following syntax (where `sortOrder` may be `asc` or `desc`):

`sort = [field1 = sortOrder, field2 = sortOrder...]`

### GroupBy
GroupBy follows the following syntax (*groupBy* does not accept *projections* parameters and is expected to be used along with an aggregation function):

`groupBy = [field1, field2, field3...]`

### Sum
It performs Sum function in the specified fields, and follows the following syntax:

`sum = [field1, field2, field3...]`

### Avg
It performs function of Avg in the specified fields, and follows the following syntax:

`avg = [field1, field2, field3...]`

### Count
It performs Count function in the specified fields, and follows the following syntax:

`count = [field1, field2, field3...]`

### Count Distinct
It performs Count Distinct function in the specified fields, and follows the following syntax:

`count_distinct = [field1, field2, field3...]`

### Extra Parameters
- offset (DEFAULT_OFFSET = 0)
- limit (DEFAULT_LIMIT = 20 and MAX_LIMIT = 100)

# ModelInvoker

Service responsible for processing message broker *RabbitMq* messages. These messages dictate whether a computational model will initiate an execution or abort an execution of a *model executor/data extractor*.
The tasks may be manageble throught the platform (the executor, extractors and the environment in which the *task/workflow/command* will execute).
There is also a *cron* task that verifies the status of models executing in Clusters.

# PhenoManagerPortal

Front-end that consumes exposed API resources. The front-end design was written using *angular 1*.
To install the project, simply run the commands in the front-end project directory:

- `npm install`
- `bower install`

Finally, just run the `gulp server` command and access the url:

`localhost: 9502`

## Gulp

Gulp is a task automator that performs some tasks on the front-end project.

- `gulp server`: runs the server locally, performing the *build* for the *dist* folder;
- `gulp build`: performs the *build* of the static files for the *dist* folder, handling the *sass* files and all the plugins and dependencies;
- `gulp build-production`: performs the same procedure as above, but running *javascripts uglify*;
