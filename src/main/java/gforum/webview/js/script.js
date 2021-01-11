
let overviewPagesInfo = {
    "my_forums" : {
        // "logo": "images/my_forums_logo.png",
        "logo": "https://i.imgur.com/NmWKXcH.png",
        "title": "My Forums",
        "desc": "Forums of the groups you are a member of",
        "index": 2
    },
    "most_active" : {
        "logo": "https://i.imgur.com/vTpk1l6.png",
        "title": "Most Active Forums",
        "desc": "Public forums arranged by number of posts in last 7 days",
        "index": 0
    },
    "most_viewed" : {
        "logo": "https://i.imgur.com/ubvaGgT.png",
        "title": "Most Viewed Forums",
        "desc": "Public forums arranged by number of unique readers in last 7 days",
        "index": 1
    }
};

function setOverview(overviewName) {
    let overview = overviewPagesInfo[overviewName];

    $("#logo").empty();
    $('#logo').prepend(`<img src="${overview["logo"]}" />`);

    $("#info_title_container").html(overview["title"]);
    $("#info_desc_container").html(overview["desc"]);

    $("#content_title").html(overview["title"]);
}

// $(window).on('load', function () {
//     for (let overviewName in overviewPagesInfo) {
//         $(`#overview_${overviewName}`).click( function(e) {e.preventDefault(); setOverview(overviewName)});
//     }
//
// });
