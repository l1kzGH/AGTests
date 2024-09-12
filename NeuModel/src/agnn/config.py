from pathlib import Path


def get_config():
    return {
        "batch_size": 8,
        "num_epochs": 100,
        "lr": 0.0001,
        "seq_len": 500,
        "d_model": 512,
        "src": "sources",
        "tgt": "tests",
        "tokenizer_file": "../../save/tokenizer_{0}.json"
    }


def get_weights_file_path():
    return str("../../save/modelname.pt")


# Find the latest weights file
def latest_weights_file_path():
    model_filename = str("../../save/modelname.pt")
    weights_files = list(Path().glob(model_filename))
    if len(weights_files) == 0:
        return None
    return str(weights_files[-1])

