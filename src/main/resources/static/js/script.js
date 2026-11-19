document.addEventListener("DOMContentLoaded", () => {
    // Add a shadow and darken the glassmorphism navbar on scroll
    const navbar = document.querySelector('.glass-nav');

    window.addEventListener('scroll', () => {
        if (window.scrollY > 50) {
            navbar.style.background = 'rgba(10, 11, 16, 0.95)';
            navbar.style.boxShadow = '0 4px 30px rgba(0, 0, 0, 0.5)';
        } else {
            navbar.style.background = 'rgba(10, 11, 16, 0.7)';
            navbar.style.boxShadow = 'none';
        }
    });
});

const search = () => {
    let query = $("#search-input").val();

    if (query == "") {
        $(".search-result").hide();
    } else {
        const resultContainer = document.querySelector(".search-result");
        let url = `${window.location.origin}/user/search/${encodeURIComponent(query)}`;

        fetch(url).then((response) => {
            if (!response.ok) {
                throw new Error("Search request failed");
            }
            return response.json();
        }).then((data) => {
            resultContainer.replaceChildren();
            if (data.length > 0) {
                data.forEach((contact) => {
                    const link = document.createElement("a");
                    link.href = `/user/contact/${encodeURIComponent(contact.cId)}`;
                    link.textContent = contact.name;
                    resultContainer.appendChild(link);
                });
            } else {
                const noResults = document.createElement("a");
                noResults.href = "#";
                noResults.textContent = "No contacts found...";
                noResults.style.pointerEvents = "none";
                noResults.style.color = "#6c757d";
                noResults.style.fontStyle = "italic";
                resultContainer.appendChild(noResults);
            }
            $(".search-result").show();
        }).catch(() => {
            resultContainer.replaceChildren();
            resultContainer.textContent = "Search is temporarily unavailable.";
            $(".search-result").show();
        })
    }
};