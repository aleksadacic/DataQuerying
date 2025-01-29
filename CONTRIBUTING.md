# Contributing to DataQuerying

First off, thanks for taking the time to contribute! ❤️

All types of contributions are encouraged and valued. See the [Table of Contents](#table-of-contents) for different ways to help and details about how this project handles them. Please make sure to read the relevant section before making your contribution. It will make it a lot easier for us maintainers and smooth out the experience for all involved. The community looks forward to your contributions. 🎉

> And if you like the project, but just don't have time to contribute, that's fine. There are other easy ways to support the project and show your appreciation, which we would also be very happy about:
> - Star the project
> - Write on social media about it
> - Refer this project in your project's readme
> - Mention the project at local meetups and tell your friends/colleagues

<!-- omit in toc -->
## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [I Want To Contribute](#i-want-to-contribute)
  - [Ask questions](#ask-questions)
  - [Submit a bug report](#submit-a-bug-report)
  - [Suggesting Enhancements](#suggesting-enhancements)
  - [Your First Code Contribution](#your-first-code-contribution)

## Code of Conduct
This project is governed by the [DataQuerying Code of Conduct](https://github.com/aleksadacic/DataQuerying/blob/master/CODE_OF_CONDUCT.md).
By participating you are expected to uphold this code.
Please report unacceptable behavior to aleksa.dacic99@gmail.com.

## I Want To Contribute

### Ask questions

If you have a question, check Stack Overflow using [specific tags](https://stackoverflow.com/questions/tagged/data-querying-for-spring). Find an existing discussion, or start a new one if necessary.

If you believe there is an issue, search through existing issues trying a few different ways to find discussions, past or current, that are related to the issue. 
Reading those discussions helps you to learn about the issue, and helps us to make a decision.

### Submit a bug report

#### Before Submitting a Bug Report

A good bug report shouldn't leave others needing to chase you up for more information. Therefore, we ask you to investigate carefully, collect information and describe the issue in detail in your report. Please complete the following steps in advance to help us fix any potential bug as fast as possible.

- Make sure that you are using the latest version.
- Determine if your bug is really a bug and not an error on your side e.g. using incompatible environment components/versions (Make sure that you have read the [documentation](https://github.com/aleksadacic/DataQuerying/blob/master/README.md). If you are looking for support, you might want to check [this section](#ask-questions)).
- To see if other users have experienced (and potentially already solved) the same issue you are having, check if there is not already a bug report existing for your bug or error in the [bug tracker](https://github.com/aleksadacic/DataQuerying/issues?q=label%3Abug).
- Also make sure to search the internet (including Stack Overflow) to see if users outside of the GitHub community have discussed the issue.
- Collect information about the bug:
  - Stack trace (Traceback)
  - OS, Platform and Version (Windows, Linux, macOS, x86, ARM)
  - Version of the interpreter, compiler, SDK, runtime environment, package manager, depending on what seems relevant.
  - Possibly your input and the output
  - Can you reliably reproduce the issue? And can you also reproduce it with older versions?

#### How Do I Submit a Good Bug Report?

> You must never report security related issues, vulnerabilities or bugs including sensitive information to the issue tracker, or elsewhere in public. Instead sensitive bugs must be sent by email to <aleksa.dacic99@gmail.com>.

We use GitHub issues to track bugs and errors. If you run into an issue with the project:

- Open an [Issue](https://github.com/aleksadacic/DataQuerying/issues/new). (Since we can't be sure at this point whether it is a bug or not, we ask you not to talk about a bug yet and not to label the issue.)
- Explain the behavior you would expect and the actual behavior.
- Please provide as much context as possible and describe the *reproduction steps* that someone else can follow to recreate the issue on their own. This usually includes your code. For good bug reports you should isolate the problem and create a reduced test case.
- Provide the information you collected in the previous section.

Once it's filed:

- The project team will label the issue accordingly.
- A team member will try to reproduce the issue with your provided steps. If there are no reproduction steps or no obvious way to reproduce the issue, the team will ask you for those steps and mark the issue as `needs-repro`. Bugs with the `needs-repro` tag will not be addressed until they are reproduced.
- If the team is able to reproduce the issue, it will be marked `needs-fix`, as well as possibly other tags (such as `critical`), and the issue will be left to be [implemented by someone](#your-first-code-contribution).


### Suggesting Enhancements

This section guides you through submitting an enhancement suggestion for DataQuerying, **including completely new features and minor improvements to existing functionality**. Following these guidelines will help maintainers and the community to understand your suggestion and find related suggestions.

#### Before Submitting an Enhancement

- Make sure that you are using the latest version.
- Read the [documentation](https://github.com/aleksadacic/DataQuerying/blob/master/README.md) carefully and find out if the functionality is already covered, maybe by an individual configuration.
- Perform a [search](https://github.com/aleksadacic/DataQuerying/issues) to see if the enhancement has already been suggested. If it has, add a comment to the existing issue instead of opening a new one.
- Find out whether your idea fits with the scope and aims of the project. It's up to you to make a strong case to convince the project's developers of the merits of this feature. Keep in mind that we want features that will be useful to the majority of our users and not just a small subset. If you're just targeting a minority of users, consider writing an add-on/plugin library.

#### How Do I Submit a Good Enhancement Suggestion?

Enhancement suggestions are tracked as [GitHub issues](https://github.com/aleksadacic/DataQuerying/issues).

- Use a **clear and descriptive title** for the issue to identify the suggestion.
- Provide a **step-by-step description of the suggested enhancement** in as many details as possible.
- **Describe the current behavior** and **explain which behavior you expected to see instead** and why. At this point you can also tell which alternatives do not work for you.
- You may want to **include screenshots** which help you demonstrate the steps or point out the part which the suggestion is related to.
- **Explain why this enhancement would be useful** to most DataQuerying users. You may also want to point out the other projects that solved it better and which could serve as inspiration.


### Your First Code Contribution
1. Fork the Repository
    - Go to the project repository on GitHub.
    - Click the Fork button in the top-right corner to create your copy of the repository.
  
2. Clone Your Fork
  ```bash
  git clone https://github.com/your-username/[repository-name].git
  cd [repository-name]
  ```
3. Create a New Branch
    - Use a descriptive name for your branch (e.g., feature/add-feature-x or bugfix/fix-issue-y):
  ```bash
  git checkout -b feature/add-feature-x
  ```
4. Make Your Changes
    - Make sure your changes adhere to the project's coding standards.
    - Add or update tests if applicable.
    - Test your changes locally to ensure they work as expected.

5. Commit Your Changes
     - Write clear and concise commit messages:
  ```bash
  git add .
  git commit -m "Add feature X to improve Y"
  ```

6. Push Your Branch to Your Fork
  ```bash
  git push origin feature/add-feature-x
  ```
Here’s a CONTRIBUTING.md template that clearly explains how users can contribute code to your project using pull requests:
Contributing to [Project Name]

Thank you for your interest in contributing to [Project Name]! We welcome contributions to improve the project, whether it's fixing bugs, adding new features, or improving documentation. Follow these steps to get started:
🚀 How to Contribute Code
1. Fork the Repository

    Go to the project repository on GitHub.
    Click the Fork button in the top-right corner to create your copy of the repository.

2. Clone Your Fork

git clone https://github.com/your-username/[repository-name].git
cd [repository-name]

3. Create a New Branch

    Use a descriptive name for your branch (e.g., feature/add-feature-x or bugfix/fix-issue-y):

git checkout -b feature/add-feature-x

4. Make Your Changes

    Make sure your changes adhere to the project's coding standards.
    Add or update tests if applicable.
    Test your changes locally to ensure they work as expected.

5. Commit Your Changes

    Write clear and concise commit messages:

git add .
git commit -m "Add feature X to improve Y"

6. Push Your Branch to Your Fork

git push origin feature/add-feature-x

7. Open a Pull Request

    1) Go to the original repository on GitHub.
    2) Click Pull Requests > New Pull Request.
    3) Select your branch and provide a detailed description of your changes:
       - Explain the problem you're solving or the feature you're adding.
       - Link to any related issues (e.g., Closes #123).

✅ Pull Request Checklist

Before submitting your pull request, make sure you:

- [ ] Have followed the coding style of the project.
- [ ] Updated documentation (if applicable).
- [ ] Added or updated tests (if applicable).
- [ ] Ensured all tests pass (npm test, mvn test, etc., depending on the project).

