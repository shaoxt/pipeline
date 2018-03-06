package v1alpha1

//
//   deepcopy-gen -O zz_generated.deepcopy --go-header-file="$GOPATH/src/k8s.io/kubernetes/hack/boilerplate/boilerplate.go.txt" -i ./
//   codecgen -o types.generated.go types.go

// +k8s:deepcopy-gen=generate,register

import (
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

// +genclient
// +nonNamespaced

// Application represents an application
type Application struct {
	metav1.TypeMeta `json:",inline"`
	metav1.ObjectMeta       `json:"metadata,omitempty"`

	Spec   ApplicationSpec   `json:"spec,omitempty"`
	Status Status `json:"status,omitempty"`
}

// Which language it is using
type Language string
const (
	Java Language = "java"
	Go   Language = "go"
	Python Language = "python"
	//Etc.
)

// Application Type, such as service, web, batch
type ApplicationType string

const (
	Service ApplicationType = "service"
	Web   ApplicationType = "web"
	Batch ApplicationType = "batch"
	//Etc.
)

// Application Spec
type ApplicationSpec struct {
	//The template this application pickup at creation time
	InitialTemplate string `json:"initialTemplate,omitempty"`
	//Application Template content for customization, if the template got changed by user
	TemplateSpec `json:"inline"`
	//Git Repo
	GitRepo GitRepo `json:"gitRepo,omitempty"`
	//Owner of the application
	Owner string `json:"owner,omitempty"`
	//Versioning scheme of the application
	VersioningScheme string `json:"versioningScheme,omitempty"`
}

type ApplicationList struct {
	metav1.TypeMeta `json:",inline"`
	metav1.ListMeta `json:"metadata"`

	Items []Application `json:"items"`
}

type GitRepo struct {
	URL string `json:"url,omitempty"`
	SharedSecret string `json:"sharedSecret,omitempty"`
}

type Status struct {
	//Phase of the application
	Phase Phase `json:"phase,omitempty"`
	//Message
	Message string `json:"message,omitempty"`
	// RFC 3339 date and time
	StartTime *metav1.Time `json:"startTime,omitempty"`
}

type Phase string

const (
	ActivePhase     Phase = "Active"
	InactivePhase   Phase = "Inactive"
	PendingPhase    Phase = "Pending"
	DeprecatedPhase Phase = "Deprecated"
	DecommissionedPhase   Phase = "Decommissioned"
)

// +genclient
// +nonNamespaced

type Template struct {
	metav1.TypeMeta `json:",inline"`
	metav1.ObjectMeta       `json:"metadata,omitempty"`

	Spec   TemplateSpec   `json:"spec,omitempty"`
	Status Status `json:"status,omitempty"`
}

type TemplateSpec struct {
	Language Language  `json:"language"`
	ApplicationType ApplicationType  `json:"applicationType"`
	SourceControl SourceControl `json:"sourceControl"`
	BuildTool BuildTool `json:"buildTool"`
	TestTool TestTool `json:"testTool,omitempty"`
	PackagingTool PackagingTool `json:"packagingTool"`
	ImageRepo ImageRepo `json:"imageRepo,omitempty"`

	Stage[] StageName `json:"stages"`

	//Versioning scheme of the pipeline
	VersioningScheme string `json:"versioningScheme,omitempty"`
}

type SourceControl string
const (
	Git    SourceControl = "Git"
	Subversion   SourceControl = "Subversion"
	CVS    SourceControl = "CVS"
	//ETC.
)

type BuildTool string
const (
	Maven    BuildTool = "Maven"
	Gradle   BuildTool = "Gradle"
	Shell    BuildTool = "Shell"
	Make     BuildTool = "make"
	Javac    BuildTool = "javac"
	//ETC.
)

type TestTool string
const (
	JUnit    TestTool = "JUnit"
	TestNG   TestTool = "TestNG"
	//ETC.
)

type PackagingTool string
const (
	Docker   PackagingTool = "Docker"
	JavaEE   PackagingTool = "JavaEE"
	//ETC.
)

type ImageRepo string
const (
	DockerHub ImageRepo = "DockerHub"
	ECR     ImageRepo = "ECR"
	Nexus   ImageRepo = "Nexus"
	//ETC.
)

type TemplateList struct {
	metav1.TypeMeta `json:",inline"`
	metav1.ListMeta `json:"metadata"`

	Items []Template `json:"items"`
}

// +genclient
// +nonNamespaced

// Pipeline
type Pipeline struct {
	metav1.TypeMeta `json:",inline"`
	metav1.ObjectMeta       `json:"metadata,omitempty"`

	Spec   PipelineSpec   `json:"spec,omitempty"`
	Status Status `json:"status,omitempty"`
}

// Pipeline Spec
type PipelineSpec struct {
	//Application
	Application string `json:"application,omitempty"`

	Stage[] StageName `json:"stages"`

	CurrentStage StageName  `json:"currentStage"`

	//Versioning scheme of the pipeline
	VersioningScheme string `json:"versioningScheme,omitempty"`
}

type StageName string

const (
	Checkout string = "Checkout"
	Build string = "Build"
	Test string = "Test"
	Packaging string = "Packaging"
	Ship string = "Ship"
	Run string = "Run"
)

type PipelineList struct {
	metav1.TypeMeta `json:",inline"`
	metav1.ListMeta `json:"metadata"`

	Items []Pipeline `json:"items"`
}