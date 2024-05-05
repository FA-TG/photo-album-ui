function previewImage(event) {
    var input = event.target;
    var imagePreview = document.getElementById('imagePreview');
    imagePreview.innerHTML = '';

    var file = input.files[0];
    var reader = new FileReader();

    reader.onload = function(event) {
        var img = document.createElement('img');
        img.src = event.target.result;
        imagePreview.appendChild(img);
    }

    reader.readAsDataURL(file);
}
