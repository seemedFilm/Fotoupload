using System.Net.Http;
using System.Net.Http.Headers;

namespace Fotoupload
{
    public partial class MainForm : Form
    {

        private string? selectedFile;
        private readonly string uploadUrl = "http://bilderrahmen/upload.php";
        private readonly string apiKey = "DEIN_GEHEIMES_API_KEY";

        public MainForm()
        {
            InitializeComponent();
        }

        private void btnSelect_Click(object sender, EventArgs e)
        {
            if (openFileDialog1.ShowDialog() == DialogResult.OK)
            {
                selectedFile = openFileDialog1.FileName;
                lblStatus.Text = $"Ausgewählt: {Path.GetFileName(selectedFile)}";
            }
        }

        private async void btnUpload_Click(object sender, EventArgs e)
        {
            if (string.IsNullOrEmpty(selectedFile))
            {
                lblStatus.Text = "Keine Datei ausgewählt!";
                return;
            }

            lblStatus.Text = "Lade hoch...";

            try
            {
                using var client = new HttpClient();
                client.DefaultRequestHeaders.Add("X-API-Key", apiKey);

                using var content = new MultipartFormDataContent();
                using var fileStream = File.OpenRead(selectedFile);
                var fileContent = new StreamContent(fileStream);
                content.Add(fileContent, "file", Path.GetFileName(selectedFile));

                var response = await client.PostAsync(uploadUrl, content);
                var result = await response.Content.ReadAsStringAsync();

                lblStatus.Text = $"Erfolg: {result}";
            }
            catch (Exception ex)
            {
                lblStatus.Text = $"Fehler: {ex.Message}";
            }
        }
    }
}
