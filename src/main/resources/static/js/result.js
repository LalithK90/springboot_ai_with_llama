
// Ensure the last message is shown on page load
$(document).ready(() => {
    initialize(); // Initialize your chat logic
    scrollToLastMessage();

    document.getElementById("message-text").addEventListener("input", function () {
        this.style.height = "auto"; // Reset height to auto so it can shrink
        this.style.height = (this.scrollHeight) + "px"; // Set height to match content

        // Check if height exceeds 25% of viewport height
        const maxHeight = window.innerHeight * 0.25; // 25% of viewport height
        if (this.scrollHeight > maxHeight) {
            this.style.height = maxHeight + "px"; // Set height to max
            this.style.overflow = "auto"; // Enable scrolling inside textarea
        } else {
            this.style.overflow = "hidden"; // Hide scrollbar if under max height
        }
    });


});

function scrollToLastMessage() {
    const messagesContainer = document.getElementById("messages");
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
}


// Manage chat history and groups in local storage
const LOCAL_STORAGE_KEY = "chatHistory";
const DEFAULT_GROUP_NAME = "Nice Chat";
const chatHistory = JSON.parse(localStorage.getItem(LOCAL_STORAGE_KEY)) || {};

let currentGroup = DEFAULT_GROUP_NAME;

// Initialize groups and messages
function initialize() {
    if (!chatHistory[DEFAULT_GROUP_NAME]) {
        chatHistory[DEFAULT_GROUP_NAME] = [];
    }
    renderGroups();
    renderMessages();
}

// Render chat groups
function renderGroups() {
    $("#groups").empty();
    Object.keys(chatHistory).forEach(group => {
        $("#groups").append(
            `<div class="chat-group" data-group="${group}">${group}</div>`
        );
    });
}

// Render messages for the current group
function renderMessages() {
    // Configure Marked.js and Highlight.js
    marked.setOptions({
        highlight: function (code, language) {
            return hljs.highlightAuto(code, [language]).value;
        }
    });

    const renderer = new marked.Renderer();
    renderer.code = function (code, language) {
        const highlightedCode = marked.defaults.highlight(code, language || 'plaintext');
        return `
                <div class="code-block-container">
                    <button class="copy-btn btn-outline-primary" onclick="copyToClipboard(this)">Copy</button>
                    <pre><code class="hljs ${language}">${highlightedCode}</code></pre>
                </div>
            `;
    };

    marked.use({renderer});

    const messages = chatHistory[currentGroup] || [];
    const messagesContainer = $("#messages");
    messagesContainer.empty();

    messages.forEach(msg => {
        const messageHtml = `
            <div class="message ${msg.sender}">
                ${marked.parse(msg.text)}
                <div class="message-actions">
                    <i class="fas fa-volume-up speaker-icon" title="Read message"></i> &nbsp;
                    <i class="fas fa-copy copy-icon" title="Copy message"></i>
                </div>
                 <div class="speech-controls d-none">
                    <label><i class="fas fa-volume-up"></i> Volume:<input type="range" class="volume-control" min="0" max="1" step="0.1" value="1">
                    </label>
                    <label><i class="fas fa-tachometer-alt"></i> Speed:<input type="range" class="speed-control" min="0.5" max="2" step="0.1" value="1">
                    </label>
                    <button class="pause-btn btn btn-sm btn-outline-warning"><i class="fas fa-pause"></i> Pause</button>
                    <button class="resume-btn btn btn-sm btn-outline-success"><i class="fas fa-play"></i> Resume</button>
                    <button class="stop-btn btn btn-sm btn-outline-danger"> <i class="fas fa-stop"></i> Stop </button>
                </div>
            </div>`;
        messagesContainer.append(messageHtml);
    });

    // Attach event listeners for speaker and copy icons
    $(".speaker-icon").click(function () {
        const messageElement = $(this).closest(".message");
        const messageText = messageElement.text().trim();
        const controls = messageElement.find(".speech-controls");
        controls.removeClass("d-none");

        let utterance = null; // Current utterance instance
        const synth = window.speechSynthesis;

        // Function to create and speak the utterance
        function speakText() {
            if (synth.speaking) synth.cancel(); // Stop ongoing speech

            utterance = new SpeechSynthesisUtterance(messageText);
            utterance.volume = parseFloat(controls.find(".volume-control").val());
            utterance.rate = parseFloat(controls.find(".speed-control").val());
            synth.speak(utterance);

            // Hide controls when speech ends
            utterance.onend = () => {
                controls.addClass("d-none");
            };
        }

        // Start speaking the text
        speakText();

        // Volume Control
        controls.find(".volume-control").off("input").on("input", function () {
            if (utterance) {
                // Update and restart speech with new volume
                speakText();
            }
        });

        // Speed Control
        controls.find(".speed-control").off("input").on("input", function () {
            if (utterance) {
                // Update and restart speech with new rate
                speakText();
            }
        });

        // Pause Button
        controls.find(".pause-btn").off("click").click(() => {
            if (synth.speaking && !synth.paused) synth.pause();
        });

        // Resume Button
        controls.find(".resume-btn").off("click").click(() => {
            if (synth.paused) synth.resume();
        });

        // Stop Button
        controls.find(".stop-btn").off("click").click(() => {
            synth.cancel();
            controls.addClass("d-none");
        });
    });

    $(".copy-icon").click(function () {
        const messageElement = $(this).closest(".message").clone(); // Clone the message element
        messageElement.find(".speech-controls").remove(); // Remove speech-controls section
        const messageText = messageElement.text().trim(); // Get the cleaned-up text

        navigator.clipboard.writeText(messageText).then(() => {
            Swal.fire({
                title: 'Copied!',
                text: "Message content copied to clipboard.",
                icon: 'success',
                timer: 2000,
                showConfirmButton: false
            });
        }).catch(() => {
            Swal.fire({
                title: 'Error!',
                text: "Failed to copy the message content.",
                icon: 'error',
                timer: 2000,
                showConfirmButton: false
            });
        });
    });


    // Scroll to the last message
    scrollToLastMessage();
}

// Copy to clipboard function
function copyToClipboard(button) {
    const codeText = button.nextElementSibling.textContent;
    navigator.clipboard.writeText(codeText).then(() => {
        button.textContent = "Copied!";
        setTimeout(() => {
            button.textContent = "Copy";
        }, 2000);
        Swal.fire({
            title: 'Copied!',
            text: 'Code block copied to clipboard.',
            icon: 'success',
            timer: 1000,
            showConfirmButton: false
        });
    }).catch(err => console.error("Copy failed:", err));
}

document.querySelectorAll('pre code').forEach(block => hljs.highlightBlock(block));

document.addEventListener('keydown', (event) => {
    if (event.metaKey && event.keyCode === 13 /* Enter */) { // macOS Command + Enter
        console.log('⌘+Enter pressed!');
        sendMessage();
    }
});

// Send message to the API
function sendMessage() {
    const text = $("#message-text").val().trim();
    if (!text) return;

    const loadingIndicator = $(`<div class="message loading"> <span></span> <span></span> <span></span> </div>`);
    $("#messages").append(loadingIndicator);
    $("#message-text").val("");

    $.ajax({
        url: `${chatUrl}=${encodeURIComponent(text)}`,
        method: "GET",
        success: function (response) {
            loadingIndicator.remove();
            chatHistory[currentGroup].push({sender: "user", text});
            chatHistory[currentGroup].push({sender: "bot", text: response});
            saveChatHistory();
            renderMessages();
            $("#message-text").val("");
        },
        error: function () {
            loadingIndicator.remove();
            Swal.fire("Error", "Failed to connect to the chat API!", "error");
        }
    });
}

// Upload file to the API using SweetAlert modal
function uploadFile() {
    Swal.fire({
        title: 'Upload a File',
        input: 'file',
        inputAttributes: {
            'accept': 'application/pdf, .xlsx, .txt, .docx,.txt,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.odt,.ods,.odp,.pdf,.epub,.html,.htm,.xml,.jpg,.jpeg,.png,.gif,.bmp,.tiff,.svg,.msg,.eml,.rtf,.csv,.tsv,.zip,.gz,.tar',
            'aria-label': 'Upload a file'
        },
        showCancelButton: true,
        confirmButtonText: 'Upload',
        showLoaderOnConfirm: true,
        preConfirm: (file) => {
            if (!file) {
                Swal.showValidationMessage('Please select a file');
                return false;
            }

            const formData = new FormData();
            formData.append("file", file);
            return $.ajax({
                url: `${fileUploadUrl}`,
                method: "POST",
                data: formData,
                processData: false,
                contentType: false,
                success: function () {
                    Swal.fire({
                        title: 'Success',
                        text: 'File uploaded successfully!',
                        icon: 'success',
                        timer: 3000, // 3000 milliseconds = 3 seconds
                        showConfirmButton: false // Hides the "OK" button
                    });

                },
                error: function () {
                    Swal.fire('Error', 'Failed to upload the file.', 'error');
                }
            });
        }
    });
}

// Clear group chat history
function clearGroupChat() {
    Swal.fire({
        title: "Are you sure?",
        text: `This will delete all messages in the "${currentGroup}" group.`,
        icon: "warning",
        showCancelButton: true,
        confirmButtonText: "Yes, clear it!"
    }).then(result => {
        if (result.isConfirmed) {
            // chatHistory[currentGroup] = [];
            Object.keys(chatHistory).some(group => {
                if (group !== 'Nice Chat' && group === currentGroup) {
                    delete chatHistory[group];
                    return true; // Breaks the iteration
                }
                chatHistory[currentGroup] = []
                return false; // Continue iteration
            });
            renderGroups()
            saveChatHistory();
            renderMessages();
            Swal.fire("Cleared!", "Group chat has been cleared.", "success");
        }
    });
}

// Save chat history to local storage
function saveChatHistory() {
    localStorage.setItem(LOCAL_STORAGE_KEY, JSON.stringify(chatHistory));
}

// Create a new group
function createNewGroup() {
    Swal.fire({
        title: 'Create New Group',
        text: 'Enter a name for the new group:',
        input: 'text', // Allows the user to input text
        inputPlaceholder: 'Group Name',
        showCancelButton: true, // Adds a cancel button
        confirmButtonText: 'Create',
        cancelButtonText: 'Cancel',
        inputValidator: (value) => {
            if (!value) {
                return 'You need to enter a group name!';
            }
            return null; // Valid input
        }
    }).then((result) => {
        if (result.isConfirmed) {
            const groupName = result.value;
            if (groupName && !chatHistory[groupName]) {
                chatHistory[groupName] = [];
                saveChatHistory();
                currentGroup = groupName;
                renderGroups();
                renderMessages();
            }
            Swal.fire({
                title: 'Success',
                text: `Group "${groupName}" created successfully!`,
                icon: 'success',
                timer: 3000, // 3000 milliseconds = 3 seconds
                showConfirmButton: false // Hides the "OK" button
            });
        } else if (result.isDismissed) {
            console.log('Group creation canceled');
        }
    });
}

// Switch to a different group
$(document).on("click", ".chat-group", function () {
    const groupName = $(this).data("group");
    currentGroup = groupName;
    renderMessages();
});

// Event listeners
$("#send-btn").click(sendMessage);
$("#file-upload-btn").click(uploadFile);
$("#clear-group-chat").click(clearGroupChat);
$("#new-group").click(createNewGroup);

function weblinkUpload() {
    Swal.fire({
        title: 'Enter Website URL',
        input: 'text',
        inputLabel: 'Website URL',
        inputPlaceholder: 'Enter the URL of the website',
        showCancelButton: true,
        confirmButtonText: 'Submit',
        cancelButtonText: 'Cancel',
        inputValidator: (value) => {
            if (!value) {
                return 'Please enter a URL!';
            }
            // Validate the URL format (simple validation)
            const urlPattern = /^(https?:\/\/[^\s$.?#].[^\s]*)$/i;
            if (!urlPattern.test(value)) {
                return 'Please enter a valid URL!';
            }
        }
    }).then((result) => {
        if (result.isConfirmed) {
            const url = result.value; // Get the entered URL

            // Send the URL to the backend using AJAX (jQuery)
            $.ajax({
                url: `${webUploadUrl}${encodeURIComponent(url)}`, // Send URL as a query parameter
                type: 'POST',
                success: function(response) {
                    // Handle the response from the backend if needed
                    console.log('Response from backend:', response);
                    Swal.fire('Success!', 'The website URL has been uploaded!', 'success');
                },
                error: function(xhr, status, error) {
                    // Handle any errors
                    console.error('Error:', error);
                    Swal.fire('Error!', 'There was a problem uploading the URL.', 'error');
                }
            });
        }
    });
}

$("#uploadButton").click(weblinkUpload);